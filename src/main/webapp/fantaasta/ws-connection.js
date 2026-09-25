/**
 * Gestione centralizzata WebSocket FantaAsta.
 * Stati: idle → connecting → open → logging_in → ready | suspended
 */
var FantaWsConnection = (function() {
	var STATE = {
		IDLE: 'idle',
		CONNECTING: 'connecting',
		OPEN: 'open',
		LOGGING_IN: 'logging_in',
		READY: 'ready',
		SUSPENDED: 'suspended'
	};

	var ws = null;
	var socketGeneration = 0;
	var activeGeneration = 0;
	var state = STATE.IDLE;
	var messageQueue = [];
	var reconnectTimer = null;
	var reconnectDelayMs = 1000;
	var connectPromise = null;
	var loginPromise = null;
	var manualClose = false;
	var suspendedByOtherTab = false;
	var stolenByRemote = false;
	var tabId = 'tab-' + Date.now() + '-' + Math.floor(Math.random() * 100000);
	var leaderChannel = null;
	var sessionUser = null;
	var sessionUserId = null;
	var tokenUtente = null;
	var loginAckDeferred = null;
	var loginTimeoutId = null;
	var loginTimeoutMs = 15000;
	var stopReconnect = false;
	var callbacks = {};

	function clearLoginTimeout() {
		if (loginTimeoutId) {
			clearTimeout(loginTimeoutId);
			loginTimeoutId = null;
		}
	}

	function scheduleLoginTimeout() {
		clearLoginTimeout();
		loginTimeoutId = setTimeout(function() {
			loginTimeoutId = null;
			if (!loginAckDeferred) {
				return;
			}
			loginAckDeferred.reject('Timeout attesa connettiOk');
			loginAckDeferred = null;
			loginPromise = null;
			setState(STATE.OPEN);
			updateStatus('Timeout login: risposta server assente. Riprova.');
		}, loginTimeoutMs);
	}

	function setState(next) {
		state = next;
		if (callbacks.onStateChange) {
			callbacks.onStateChange(next);
		}
	}

	function updateStatus(message) {
		if (callbacks.onStatus) {
			callbacks.onStatus(message);
		}
	}

	function initTabChannel() {
		if (typeof BroadcastChannel === 'undefined') {
			return;
		}
		leaderChannel = new BroadcastChannel('fantaasta-ws-leader');
		leaderChannel.onmessage = function(event) {
			var data = event.data || {};
			if (data.type !== 'ACTIVE' || data.tabId === tabId) {
				return;
			}
			if (!sessionUser || data.user !== sessionUser) {
				return;
			}
			suspendedByOtherTab = true;
			manualClose = true;
			closeSocketInternal();
			setState(STATE.SUSPENDED);
			updateStatus('Sessione attiva in un\'altra scheda del browser');
		};
	}

	function claimLeader(user) {
		if (!leaderChannel || !user) {
			return;
		}
		leaderChannel.postMessage({ type: 'ACTIVE', tabId: tabId, user: user });
	}

	function releaseLeader(user) {
		if (!leaderChannel || !user) {
			return;
		}
		leaderChannel.postMessage({ type: 'RELEASE', tabId: tabId, user: user });
	}

	function buildWsUrl() {
		var loc = window.location;
		var scheme = loc.protocol === 'https:' ? 'wss:' : 'ws:';
		document.cookie = 'PAGINA=' + window.location.href + '; path=/';
		return scheme + '//' + loc.host + '/messaggi-websocket';
	}

	function flushQueue() {
		if (state !== STATE.READY || !ws || ws.readyState !== WebSocket.OPEN) {
			return;
		}
		while (messageQueue.length > 0) {
			ws.send(messageQueue.shift());
		}
	}

	function enqueue(payload) {
		messageQueue.push(payload);
	}

	function closeSocketInternal() {
		if (!ws) {
			return;
		}
		var oldWs = ws;
		ws = null;
		oldWs.onopen = null;
		oldWs.onmessage = null;
		oldWs.onerror = null;
		oldWs.onclose = null;
		try {
			if (oldWs.readyState === WebSocket.OPEN || oldWs.readyState === WebSocket.CONNECTING) {
				oldWs.close();
			}
		} catch (e) {
			console.log('chiusura ws: ' + e);
		}
	}

	function scheduleReconnect() {
		if (manualClose || suspendedByOtherTab || stolenByRemote || stopReconnect || reconnectTimer || connectPromise) {
			return;
		}
		if (!sessionUser) {
			return;
		}
		updateStatus('Riconnessione in ' + (reconnectDelayMs / 1000) + 's...');
		reconnectTimer = setTimeout(function() {
			reconnectTimer = null;
			if (!callbacks.onReconnect) {
				return;
			}
			callbacks.onReconnect().then(function() {
				reconnectDelayMs = Math.min(reconnectDelayMs * 2, 30000);
			}, function() {
				scheduleReconnect();
			});
		}, reconnectDelayMs);
	}

	function openSocket() {
		var deferred = callbacks.defer ? callbacks.defer() : null;
		if (reconnectTimer) {
			clearTimeout(reconnectTimer);
			reconnectTimer = null;
		}
		manualClose = true;
		closeSocketInternal();
		socketGeneration += 1;
		var generation = socketGeneration;
		activeGeneration = generation;
		setState(STATE.CONNECTING);
		updateStatus('Connessione al backend in corso...');

		var socket = new WebSocket(buildWsUrl());
		ws = socket;

		socket.onopen = function() {
			if (generation !== socketGeneration) {
				return;
			}
			reconnectDelayMs = 1000;
			manualClose = false;
			setState(STATE.OPEN);
			updateStatus('');
			if (deferred) {
				deferred.resolve('ok');
			}
		};

		socket.onmessage = function(event) {
			if (generation !== activeGeneration) {
				return;
			}
			if (!callbacks.onMessage) {
				return;
			}
			callbacks.onMessage(event.data, generation);
		};

		socket.onerror = function() {
			if (generation !== socketGeneration) {
				return;
			}
			updateStatus('Errore di connessione WebSocket');
		};

		socket.onclose = function() {
			if (ws === socket) {
				ws = null;
			}
			if (generation !== activeGeneration) {
				return;
			}
			if (state === STATE.READY || state === STATE.LOGGING_IN || state === STATE.OPEN) {
				setState(STATE.IDLE);
			}
			if (!manualClose) {
				scheduleReconnect();
			}
		};

		setTimeout(function() {
			if (socket.readyState !== WebSocket.OPEN && generation === socketGeneration && deferred) {
				deferred.reject('timeout');
			}
		}, 10000);

		return deferred ? deferred.promise : null;
	}

	function ensureSocket() {
		if (suspendedByOtherTab || stolenByRemote) {
			return callbacks.rejectPromise
				? callbacks.rejectPromise('suspended')
				: Promise.reject('suspended');
		}
		if (ws && ws.readyState === WebSocket.OPEN) {
			return callbacks.resolvePromise ? callbacks.resolvePromise('ok') : Promise.resolve('ok');
		}
		if (connectPromise) {
			return connectPromise;
		}
		var p = openSocket();
		connectPromise = p.then(function(v) {
			connectPromise = null;
			return v;
		}, function(e) {
			connectPromise = null;
			throw e;
		});
		return connectPromise;
	}

	function sendRaw(payload) {
		if (ws && ws.readyState === WebSocket.OPEN) {
			ws.send(payload);
			updateStatus('');
			return true;
		}
		return false;
	}

	function parseOperazione(payload) {
		try {
			return JSON.parse(payload).operazione;
		} catch (e) {
			return null;
		}
	}

	function send(payload) {
		if (suspendedByOtherTab || stolenByRemote) {
			return callbacks.resolvePromise ? callbacks.resolvePromise(false) : Promise.resolve(false);
		}
		var op = parseOperazione(payload);
		var needsReady = op && op !== 'connetti';

		if (needsReady && state !== STATE.READY) {
			enqueue(payload);
			return ensureSocket().then(function() {
				if (sessionUser && state !== STATE.READY && state !== STATE.LOGGING_IN) {
					return login(sessionUser, sessionUserId);
				}
				return null;
			});
		}

		if (!sendRaw(payload)) {
			enqueue(payload);
			return ensureSocket();
		}
		return callbacks.resolvePromise ? callbacks.resolvePromise(true) : Promise.resolve(true);
	}

	function handleDisconnectAll() {
		stopReconnect = true;
		manualClose = true;
		clearLoginTimeout();
		if (reconnectTimer) {
			clearTimeout(reconnectTimer);
			reconnectTimer = null;
		}
		releaseLeader(sessionUser);
		sessionUser = null;
		sessionUserId = null;
		messageQueue = [];
		if (loginAckDeferred) {
			loginAckDeferred.reject('disconnect_all');
			loginAckDeferred = null;
		}
		loginPromise = null;
		closeSocketInternal();
		setState(STATE.SUSPENDED);
		updateStatus('Sessione terminata dall\'admin');
		if (callbacks.onDisconnectAll) {
			callbacks.onDisconnectAll();
		}
	}

	function login(nome, id) {
		if (!nome) {
			return callbacks.resolvePromise ? callbacks.resolvePromise(null) : Promise.resolve(null);
		}
		stopReconnect = false;
		sessionUser = nome;
		sessionUserId = id;
		suspendedByOtherTab = false;
		stolenByRemote = false;
		tokenUtente = new Date().getTime();
		claimLeader(nome);

		if (loginPromise) {
			return loginPromise;
		}

		loginAckDeferred = callbacks.defer ? callbacks.defer() : null;
		var lp = ensureSocket().then(function() {
			setState(STATE.LOGGING_IN);
			var payload = JSON.stringify({
				operazione: 'connetti',
				nomegiocatore: nome,
				idgiocatore: id,
				tokenUtente: tokenUtente
			});
			if (!sendRaw(payload)) {
				enqueue(payload);
			}
			if (loginAckDeferred) {
				scheduleLoginTimeout();
				return loginAckDeferred.promise;
			}
		});
		loginPromise = lp.then(function(v) {
			loginPromise = null;
			return v;
		}, function(e) {
			loginPromise = null;
			throw e;
		});

		return loginPromise;
	}

	function handleServerMessage(msg) {
		if (msg.DISCONNECT_ALL) {
			handleDisconnectAll();
			return;
		}
		if (msg.erroreConnetti) {
			clearLoginTimeout();
			setState(STATE.OPEN);
			updateStatus(msg.erroreConnetti);
			if (loginAckDeferred) {
				loginAckDeferred.reject(msg.erroreConnetti);
				loginAckDeferred = null;
			}
			return;
		}
		if (msg.erroreOperazione) {
			updateStatus(msg.erroreOperazione);
			return;
		}
		if (msg.connettiOk) {
			clearLoginTimeout();
			setState(STATE.READY);
			flushQueue();
			updateStatus('');
			if (loginAckDeferred) {
				loginAckDeferred.resolve('ok');
				loginAckDeferred = null;
			}
			return;
		}
		if (msg.RESET_UTENTE) {
			if (msg.RESET_UTENTE === tokenUtente) {
				stolenByRemote = true;
				manualClose = true;
				closeSocketInternal();
				sessionUser = null;
				sessionUserId = null;
				setState(STATE.SUSPENDED);
				updateStatus('Utente connesso da un altro dispositivo');
				if (callbacks.onRemoteSteal) {
					callbacks.onRemoteSteal();
				}
			}
			return;
		}
	}

	function syncFromHttpSession(nome, id) {
		if (!nome) {
			sessionUser = null;
			sessionUserId = null;
			return callbacks.resolvePromise ? callbacks.resolvePromise(null) : Promise.resolve(null);
		}
		if (suspendedByOtherTab || stolenByRemote) {
			return callbacks.resolvePromise ? callbacks.resolvePromise(null) : Promise.resolve(null);
		}
		if (state === STATE.READY && sessionUser === nome) {
			return callbacks.resolvePromise ? callbacks.resolvePromise('ok') : Promise.resolve('ok');
		}
		return login(nome, id);
	}

	function disconnect() {
		stopReconnect = false;
		manualClose = true;
		clearLoginTimeout();
		if (reconnectTimer) {
			clearTimeout(reconnectTimer);
			reconnectTimer = null;
		}
		releaseLeader(sessionUser);
		sessionUser = null;
		sessionUserId = null;
		messageQueue = [];
		closeSocketInternal();
		setState(STATE.IDLE);
		updateStatus('');
	}

	function init(options) {
		callbacks = options || {};
		initTabChannel();
		setState(STATE.IDLE);
	}

	return {
		STATE: STATE,
		init: init,
		connect: ensureSocket,
		login: login,
		syncFromHttpSession: syncFromHttpSession,
		send: send,
		disconnect: disconnect,
		handleDisconnectAll: handleDisconnectAll,
		handleServerMessage: handleServerMessage,
		getState: function() { return state; },
		isReady: function() { return state === STATE.READY; },
		isSuspended: function() { return suspendedByOtherTab || stolenByRemote; },
		getTokenUtente: function() { return tokenUtente; },
		getSessionUser: function() { return sessionUser; }
	};
})();
