/**
 * FantaAsta S14 — UI helpers (presentation only, no business logic).
 */
(function() {
	'use strict';

	var TEAM_COLORS = [
		'#E8A838', '#3ECFB2', '#7B9CFF', '#F07167', '#C084FC',
		'#34D399', '#FB923C', '#38BDF8', '#F472B6', '#A3E635',
		'#FBBF24', '#818CF8'
	];

	var BOOTSTRAP_ADMIN_NOME = 'GIOC0';
	var BOOTSTRAP_ADMIN_ID = 0;

	angular.module('app').run(['$rootScope', '$q', '$resource', '$timeout', function($rootScope, $q, $resource, $timeout) {
		$rootScope.uiTab = 'asta';
		$rootScope.opsTab = 'log';
		$rootScope.wizardStep = 1;
		$rootScope.showSettings = false;
		$rootScope.isAdminBootstrap = false;
		$rootScope.setupInProgress = false;
		$rootScope.setupTeamsSaved = false;
		$rootScope.setupCompletato = false;
		$rootScope.wizardBusy = false;
		$rootScope.wizardUploadCount = null;
		$rootScope.userMenuOpen = false;
		$rootScope.adminLoginNome = 'GIOC0';
		$rootScope.numeroUtenti = 8;
		$rootScope.wizardSlotPreview = [];

		function faViewportSize() {
			var w = window.innerWidth || 0;
			var h = window.innerHeight || 0;
			try {
				if (window.visualViewport && window.visualViewport.width) {
					w = Math.round(window.visualViewport.width) || w;
					h = Math.round(window.visualViewport.height) || h;
				}
			} catch (e) {}
			return { w: w, h: h };
		}

		/** Telefono/tablet reale da UA/touch. Non include ?mobile=1 (solo layout debug). */
		function faIsPhoneDevice() {
			try {
				var ua = navigator.userAgent || navigator.vendor || '';
				if (/Android.+Mobile|iPhone|iPod|Windows Phone|webOS|BlackBerry|IEMobile|Opera Mini/i.test(ua)) {
					return true;
				}
				if (/iPad/i.test(ua)) {
					return true;
				}
				if (navigator.platform === 'MacIntel' && (navigator.maxTouchPoints || 0) > 1) {
					return true;
				}
				if (/Android/i.test(ua) && !/Windows NT/i.test(ua)) {
					return true;
				}
				return false;
			} catch (e) {
				return false;
			}
		}

		function faIsLandscapeViewport() {
			try {
				var s = faViewportSize();
				return s.w > s.h || Math.abs(window.orientation || 0) === 90;
			} catch (e) {
				return false;
			}
		}

		/** Layout responsive stretto (tab bar, split). Su PC ridimensionato OK; non implica "telefono". */
		function faIsMobileViewport() {
			try {
				if (document.documentElement.classList.contains('fa-force-mobile')) {
					return true;
				}
				var s = faViewportSize();
				if (Math.min(s.w, s.h) < 520) {
					return true;
				}
				if (window.matchMedia && window.matchMedia('(max-width: 899px)').matches) {
					return true;
				}
				return s.w < 900;
			} catch (e) {
				return (window.innerWidth || 0) < 900;
			}
		}

		function faTryLockPortrait() {
			try {
				if (!faIsPhoneDevice()) {
					return;
				}
				if (screen.orientation && typeof screen.orientation.lock === 'function') {
					var p = screen.orientation.lock('portrait');
					if (p && typeof p.catch === 'function') {
						p.catch(function() {});
					}
				}
			} catch (e) {}
		}

		function faSyncOrientationClass() {
			try {
				var s = faViewportSize();
				var land = faIsLandscapeViewport();
				var phone = faIsPhoneDevice();
				document.documentElement.classList.toggle('fa-ui-landscape', land);
				document.documentElement.classList.toggle('fa-ui-portrait', !land);
				// Overlay ruota + layout phone-landscape: solo dispositivo mobile reale (mai ?mobile=1 su PC)
				document.documentElement.classList.toggle('fa-ui-mobile-land', !!(land && phone));
				document.documentElement.style.setProperty('--fa-vw', s.w + 'px');
				document.documentElement.style.setProperty('--fa-vh', s.h + 'px');
				if (phone) {
					faTryLockPortrait();
				}
			} catch (e) {}
		}

		$rootScope.isMobilePortraitUi = function() {
			// ?mobile=1: forza UI portrait mobile anche se la finestra PC è landscape
			if (document.documentElement.classList.contains('fa-force-mobile')) {
				return true;
			}
			return faIsMobileViewport() && !faIsLandscapeViewport();
		};

		$rootScope.isMobileLandscapeUi = function() {
			// Landscape telefono reale; con ?mobile=1 restiamo in modalità portrait debug
			if (document.documentElement.classList.contains('fa-force-mobile')) {
				return false;
			}
			return faIsPhoneDevice() && faIsLandscapeViewport();
		};

		$rootScope.isPhoneDeviceUi = function() {
			return faIsPhoneDevice();
		};

		// Rotazione telefono: classe html + digest Angular
		(function bindUiOrientationDigest() {
			var scheduled = false;
			var kick = function() {
				faSyncOrientationClass();
				if (scheduled) return;
				scheduled = true;
				$timeout(function() {
					scheduled = false;
					faSyncOrientationClass();
				}, 0);
			};
			faSyncOrientationClass();
			try {
				window.addEventListener('orientationchange', function() {
					kick();
					$timeout(kick, 80);
					$timeout(kick, 320);
				});
				window.addEventListener('resize', kick);
			} catch (e) {}
		})();

		/** Tastierino numerico iOS senza Enter → serve il tasto Invia */
		$rootScope.isIosUi = function() {
			try {
				var ua = navigator.userAgent || navigator.vendor || '';
				if (/iPad|iPhone|iPod/.test(ua)) return true;
				// iPadOS 13+ può presentarsi come Mac con touch
				if (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1) return true;
				return false;
			} catch (e) {
				return false;
			}
		};
		var NOME_LEGA_KEY = 'fantaasta-nome-lega';
		$rootScope.persistNomeLega = function(nome) {
			var v = (nome || '').toString().trim();
			try {
				if (v) {
					localStorage.setItem(NOME_LEGA_KEY, v);
				} else {
					localStorage.removeItem(NOME_LEGA_KEY);
				}
			} catch (e) {}
		};
		$rootScope.loadPersistedNomeLega = function() {
			try {
				return localStorage.getItem(NOME_LEGA_KEY) || '';
			} catch (e) {
				return '';
			}
		};
		$rootScope.nomeLega = $rootScope.loadPersistedNomeLega() || 'FantaAsta';
		$rootScope.$watch('nomeLega', function(n, o) {
			if (n === o) {
				return;
			}
			$rootScope.persistNomeLega(n);
		});
		$rootScope.playerSortField = 'quotazione';
		$rootScope.playerSortRev = true;
		$rootScope.filterNome = '';
		$rootScope.filterRuolo = '';
		$rootScope.filterSquadra = '';
		$rootScope.filterMacroRuolo = '';
		$rootScope.filterPreferito = false;
		$rootScope.squadreFilterOptions = [];

		function rebuildSquadreFilterOptions() {
			var seen = {};
			var list = [];
			angular.forEach($rootScope.calciatori || [], function(c) {
				var s = c && c.squadra;
				if (s && !seen[s]) {
					seen[s] = true;
					list.push(s);
				}
			});
			list.sort();
			$rootScope.squadreFilterOptions = list;
		}

		$rootScope.$watch('calciatori', rebuildSquadreFilterOptions, true);
		$rootScope.opsPanelOpen = false;
		$rootScope.loginUtenteSelezionatoId = null;
		$rootScope.loginUtentiOrdinatiList = [];

		if (window.location.search.indexOf('settings=1') >= 0) {
			$rootScope.showSettings = true;
		}

		$rootScope.teamColor = function(ordine) {
			var i = parseInt(ordine, 10) || 0;
			return TEAM_COLORS[i % TEAM_COLORS.length];
		};

		$rootScope.teamInitials = function(utente) {
			var nome = (utente && (utente.nuovoNome || utente.nome) || '').trim();
			if (!nome) return '?';
			var parts = nome.split(/\s+/);
			if (parts.length > 1) {
				return (parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
			}
			return nome.substring(0, 2).toUpperCase();
		};

		$rootScope.roleClass = function(ruolo) {
			if (!ruolo) return 'role-unknown';
			var r = ('' + ruolo).toUpperCase().charAt(0);
			if (r === 'P') return 'role-p';
			if (r === 'D') return 'role-d';
			if (r === 'C') return 'role-c';
			if (r === 'A') return 'role-a';
			return 'role-unknown';
		};

		$rootScope.faseAstaLabel = function() {
			switch ($rootScope.faseAsta) {
				case 'BIDDING': return 'LIVE';
				case 'DA_CONFERMARE': return 'CONFERMA';
				case 'PAUSA': return 'PAUSA';
				case 'IDLE': return $rootScope.timeout ? 'PAUSA' : 'ATTESA';
				default: return 'ATTESA';
			}
		};

		$rootScope.isAstaLive = function() {
			return $rootScope.faseAsta === 'BIDDING'
				|| $rootScope.faseAsta === 'DA_CONFERMARE'
				|| $rootScope.faseAsta === 'PAUSA';
		};

		$rootScope.isAstaInPausa = function() {
			return $rootScope.faseAsta === 'PAUSA' || !!$rootScope.timeout;
		};

		function isSetupAdmin() {
			return $rootScope.isAdmin || $rootScope.isAdminBootstrap;
		}

		function legaCreata() {
			return !$rootScope.config
				&& $rootScope.elencoAllenatori
				&& $rootScope.elencoAllenatori.length > 0;
		}

		$rootScope.isLegaCreata = legaCreata;

		$rootScope.needsWizard = function() {
			if (!$rootScope.nomegiocatore) return false;
			if ($rootScope.showSettings && $rootScope.isAdmin) return true;
			if (!isSetupAdmin()) return false;
			// Fonte di verità: lega senza quotazioni → upload obbligatorio (ignora flag locali)
			if (legaCreata() && (!$rootScope.calciatori || $rootScope.calciatori.length === 0)) {
				return true;
			}
			if ($rootScope.setupCompletato) return false;
			// Wizard creazione lega: DA_CONFIGURARE o sessione setup in corso
			if ($rootScope.config || $rootScope.setupInProgress) return true;
			return false;
		};

		$rootScope.isSetupLogin = function() {
			return !!$rootScope.config;
		};

		$rootScope.showLoginScreen = function() {
			if ($rootScope.nomegiocatore || $rootScope.needsWizard()) {
				return false;
			}
			if ($rootScope.isSetupLogin()) return true;
			return $rootScope.elencoAllenatori && $rootScope.elencoAllenatori.length > 0;
		};

		function ensureSlotDefaultNames() {
			angular.forEach($rootScope.elencoAllenatori || [], function(u) {
				if (!u.nuovoNome) {
					u.nuovoNome = u.nome;
				}
			});
		}

		function idsUguali(a, b) {
			return a != null && b != null && String(a) === String(b);
		}

		function trovaUtentePerId(id) {
			if (id == null) return null;
			var found = null;
			angular.forEach($rootScope.elencoAllenatori || [], function(u) {
				if (idsUguali(u.id, id)) {
					found = u;
				}
			});
			return found;
		}

		function nomeAccesso(utente) {
			if (!utente) return '';
			return (utente.nuovoNome || utente.nome || '').trim();
		}

		function trovaUtentePerAccesso(last) {
			if (!last) return null;
			var found = trovaUtentePerId(last.id);
			if (found) return found;
			angular.forEach($rootScope.elencoAllenatori || [], function(u) {
				if (last.nome && u.nome === last.nome) {
					found = u;
				}
			});
			return found;
		}

		function rebuildLoginUtentiOrdinati() {
			if ($rootScope.isSetupLogin && $rootScope.isSetupLogin()) {
				$rootScope.loginUtentiOrdinatiList = [];
				$rootScope.loginUtenteSelezionatoId = null;
				return;
			}
			var list = ($rootScope.elencoAllenatori || []).slice().sort(function(a, b) {
				return (a.ordine || 0) - (b.ordine || 0);
			});
			var last = $rootScope.recuperaUtenteTab && $rootScope.recuperaUtenteTab();
			var preferito = trovaUtentePerAccesso(last);
			var rest = [];
			angular.forEach(list, function(u) {
				if (!preferito || !idsUguali(u.id, preferito.id)) {
					rest.push(u);
				}
			});
			$rootScope.loginUtentiOrdinatiList = preferito ? [preferito].concat(rest) : list;
			if ($rootScope.loginUtenteSelezionatoId == null) {
				var first = $rootScope.loginUtentiOrdinatiList[0];
				$rootScope.loginUtenteSelezionatoId = first ? first.id : null;
				return;
			}
			if (!trovaUtentePerId($rootScope.loginUtenteSelezionatoId)) {
				var fallback = $rootScope.loginUtentiOrdinatiList[0];
				$rootScope.loginUtenteSelezionatoId = fallback ? fallback.id : null;
			}
		}

		$rootScope.loginUtenteSelezionato = function() {
			return trovaUtentePerId($rootScope.loginUtenteSelezionatoId);
		};

		$rootScope.nomeSquadra = function(utente) {
			return nomeAccesso(utente);
		};

		$rootScope.loginEtichetta = function(utente) {
			var nome = nomeAccesso(utente);
			if ($rootScope.isUtenteAdmin(utente)) {
				return nome + ' (Admin)';
			}
			if ($rootScope.isUtenteConnesso(nome)) {
				return nome + ' — occupato';
			}
			return nome;
		};

		$rootScope.entraComeLoginSelezionato = function() {
			var selectedId = $rootScope.loginUtenteSelezionatoId;
			if (selectedId == null) return $q.when();
			return $rootScope.entraCome(selectedId);
		};

		$rootScope.refreshLoginElenco = function() {
			if (!$rootScope.isLegaCreata || $rootScope.isSetupLogin()) {
				return $q.when();
			}
			return $resource('./init', {}).get().$promise.then(function(data) {
				if (data.elencoAllenatori) {
					$rootScope.elencoAllenatori = data.elencoAllenatori;
				}
				if (data.numeroGiocatori != null) {
					$rootScope.numeroGiocatori = data.numeroGiocatori;
				}
				if (data.utenti) {
					$rootScope.utenti = data.utenti;
				}
				rebuildLoginUtentiOrdinati();
				return data;
			});
		};

		var SETUP_TEAMS_KEY = 'fantaasta-setup-teams-saved';
		var SETUP_COMPLETE_KEY = 'fantaasta-setup-complete';

		function markSetupTeamsSaved(saved) {
			$rootScope.setupTeamsSaved = !!saved;
			try {
				if (saved) {
					sessionStorage.setItem(SETUP_TEAMS_KEY, '1');
				} else {
					sessionStorage.removeItem(SETUP_TEAMS_KEY);
				}
			} catch (e) {}
		}

		function markSetupComplete(complete) {
			$rootScope.setupCompletePersisted = !!complete;
			try {
				if (complete) {
					localStorage.setItem(SETUP_COMPLETE_KEY, '1');
				} else {
					localStorage.removeItem(SETUP_COMPLETE_KEY);
				}
			} catch (e) {}
		}

		function isSetupCompletePersisted() {
			try {
				return localStorage.getItem(SETUP_COMPLETE_KEY) === '1';
			} catch (e) {
				return false;
			}
		}

		function loadSetupTeamsSaved() {
			try {
				$rootScope.setupTeamsSaved = sessionStorage.getItem(SETUP_TEAMS_KEY) === '1';
			} catch (e) {
				$rootScope.setupTeamsSaved = false;
			}
			$rootScope.setupCompletePersisted = isSetupCompletePersisted();
		}

		function teamsPresentOnServer() {
			if (!legaCreata()) {
				return false;
			}
			var teams = $rootScope.elencoAllenatori || [];
			if (teams.length < 2) {
				return false;
			}
			var required = parseInt($rootScope.numeroGiocatori, 10) || 0;
			if (required >= 2 && teams.length < required) {
				return false;
			}
			var hasAdmin = false;
			angular.forEach(teams, function(u) {
				if ($rootScope.isUtenteAdmin(u)) {
					hasAdmin = true;
				}
			});
			return hasAdmin;
		}

		function canSkipToUploadStep() {
			return $rootScope.setupTeamsSaved || teamsPresentOnServer();
		}

		loadSetupTeamsSaved();
		$rootScope.markSetupTeamsSaved = markSetupTeamsSaved;
		$rootScope.markSetupComplete = markSetupComplete;

		$rootScope.wizardSoloUpload = function() {
			return !$rootScope.showSettings
				&& !$rootScope.config
				&& !$rootScope.setupInProgress
				&& canSkipToUploadStep()
				&& legaCreata()
				&& (!$rootScope.calciatori || $rootScope.calciatori.length === 0);
		};

		function syncWizardStepAfterInit() {
			if (!$rootScope.nomegiocatore || !$rootScope.needsWizard()) return;
			if ($rootScope.showSettings) return;
			if ($rootScope.setupInProgress) return;
			if ($rootScope.config) {
				$rootScope.wizardStep = 1;
				return;
			}
			if (!legaCreata() || ($rootScope.calciatori && $rootScope.calciatori.length > 0)) return;
			if (canSkipToUploadStep()) {
				$rootScope.wizardStep = 5;
				if (teamsPresentOnServer() && !$rootScope.setupTeamsSaved) {
					markSetupTeamsSaved(true);
				}
			} else {
				$rootScope.wizardStep = 4;
				ensureSlotDefaultNames();
			}
		}

		$rootScope.syncWizardStepAfterInit = syncWizardStepAfterInit;

		angular.element(document).on('click', function() {
			if ($rootScope.userMenuOpen) {
				$rootScope.$applyAsync(function() {
					$rootScope.userMenuOpen = false;
				});
			}
		});

		(function applyHashTab() {
			var h = (window.location.hash || '').replace('#', '');
			if (h === 'players' || h === 'teams' || h === 'asta' || h === 'ops') {
				$rootScope.uiTab = h;
			}
		})();

		$rootScope.setUiTab = function(tab) {
			$rootScope.uiTab = tab;
			if (tab === 'ops') {
				$rootScope.setOpsTab($rootScope.opsTab || 'log');
			}
			if (tab === 'teams' && $rootScope.consumaBadgeAcquistoTab) {
				$rootScope.consumaBadgeAcquistoTab();
			}
		};

		$rootScope.setOpsTab = function(tab) {
			$rootScope.opsTab = tab === 'offerte' ? 'offerte' : 'log';
			if ($rootScope.opsTab === 'offerte' && $rootScope.aggiornaCronologiaOfferte) {
				$rootScope.aggiornaCronologiaOfferte();
			}
		};

		$rootScope.syncRosaMinMax = function() {
			if ($rootScope.isMantra) {
				return;
			}
			$rootScope.minP = $rootScope.maxP;
			$rootScope.minD = $rootScope.maxD;
			$rootScope.minC = $rootScope.maxC;
			$rootScope.minA = $rootScope.maxA;
		};

		var settingsSnapshot = null;
		var SETTINGS_FIRST_STEP = 2;
		var SETTINGS_LAST_STEP = 5;

		$rootScope.isSettingsLastStep = function() {
			return !!$rootScope.showSettings && ($rootScope.wizardStep || 0) === SETTINGS_LAST_STEP;
		};

		$rootScope.wizardFooter = {
			prev: false,
			cancel: false,
			exit: 'none',
			exitDisabled: false,
			hint: false,
			primary: 'none',
			primaryLabel: '',
			primaryDisabled: false
		};

		function wizardHasQuotazioni() {
			return ($rootScope.calciatori && $rootScope.calciatori.length > 0)
				|| ($rootScope.wizardUploadCount > 0);
		}

		function uploadStepAllowsHome() {
			if ($rootScope.wizardUploadCount > 0) {
				return true;
			}
			if ($rootScope.wizardSoloUpload && $rootScope.wizardSoloUpload()) {
				return wizardHasQuotazioni();
			}
			if ($rootScope.setupInProgress) {
				return false;
			}
			return wizardHasQuotazioni();
		}

		function updateWizardFooterState() {
			var footer = {
				prev: false,
				cancel: false,
				exit: 'none',
				exitDisabled: false,
				hint: false,
				primary: 'none',
				primaryLabel: 'Avanti',
				primaryDisabled: false
			};
			var step = $rootScope.wizardStep || 1;
			var busy = !!$rootScope.wizardBusy;
			var settings = !!$rootScope.showSettings;
			var soloUpload = $rootScope.wizardSoloUpload && $rootScope.wizardSoloUpload();
			var dirty = settings && isSettingsDirty();
			var hasQuotes = wizardHasQuotazioni();

			if (settings) {
				footer.hint = dirty;
				if (dirty) {
					footer.cancel = true;
					footer.primary = 'save';
					footer.primaryLabel = busy ? 'Salvataggio…' : 'Salva';
					footer.primaryDisabled = busy;
				} else {
					if (step > SETTINGS_FIRST_STEP) {
						footer.prev = true;
					}
					if (step < SETTINGS_LAST_STEP) {
						footer.primary = 'nextSettings';
						footer.primaryLabel = 'Avanti';
						footer.primaryDisabled = busy;
						footer.exit = hasQuotes ? 'home' : 'close';
						footer.exitDisabled = busy;
					} else if (uploadStepAllowsHome()) {
						footer.exit = 'home';
						footer.exitDisabled = busy;
					} else {
						footer.exit = 'close';
						footer.exitDisabled = busy;
					}
				}
			} else {
				if (step > 1 && ($rootScope.config || $rootScope.setupInProgress || soloUpload)) {
					footer.prev = true;
				}
				if (step < 4 && !soloUpload) {
					footer.primary = 'nextCreate';
					if (busy && step === 3) {
						footer.primaryLabel = 'Creazione squadre…';
					} else if (step === 3) {
						footer.primaryLabel = 'Crea squadre';
					} else {
						footer.primaryLabel = 'Avanti';
					}
					footer.primaryDisabled = busy || (step === 3 && (($rootScope.numeroUtenti || 0) < 2 || ($rootScope.durataAstaDefault || 0) < 2));
				} else if (step === 4) {
					footer.primary = 'teams';
					footer.primaryLabel = busy ? 'Salvataggio…' : 'Salva squadre';
					footer.primaryDisabled = busy;
				} else if (step === 5) {
					footer.primary = 'homeCreate';
					footer.primaryLabel = 'Vai in home';
					footer.primaryDisabled = busy || !uploadStepAllowsHome();
				}
			}
			$rootScope.wizardFooter = footer;
		}

		$rootScope.wizardHasQuotazioni = wizardHasQuotazioni;

		$rootScope.goHomeFromSettings = function() {
			if (!$rootScope.showSettings || $rootScope.wizardBusy || isSettingsDirty()) {
				return $q.when();
			}
			if ($rootScope.wizardStep === SETTINGS_LAST_STEP && !uploadStepAllowsHome()) {
				return $q.when();
			}
			return $rootScope.closeSettingsPanel();
		};

		$rootScope.wizardFooterExitAction = function() {
			if ($rootScope.wizardBusy || $rootScope.wizardFooter.exitDisabled) {
				return $q.when();
			}
			if ($rootScope.wizardFooter.exit === 'home') {
				return $rootScope.goHomeFromSettings();
			}
			if ($rootScope.wizardFooter.exit === 'close') {
				return $rootScope.closeSettingsPanel();
			}
			return $q.when();
		};

		$rootScope.wizardFooterPrimaryAction = function() {
			if ($rootScope.wizardBusy || $rootScope.wizardFooter.primaryDisabled) {
				return $q.when();
			}
			var primary = $rootScope.wizardFooter.primary;
			if (primary === 'save') {
				return $rootScope.saveSettings();
			}
			if (primary === 'nextSettings' || primary === 'nextCreate' || primary === 'teams') {
				return $rootScope.wizardNext();
			}
			if (primary === 'homeCreate') {
				return $rootScope.wizardCompleteSetup();
			}
			return $q.when();
		};

		function toSettingsInt(value, fallback) {
			var n = parseInt(value, 10);
			return isNaN(n) ? fallback : n;
		}

		function readInputInt(selector, fallback) {
			var el = document.querySelector(selector);
			if (!el || el.value === '' || el.value == null) {
				return fallback;
			}
			return toSettingsInt(el.value, fallback);
		}

		function isWizardConfigOverlayActive() {
			if ($rootScope.showSettings || $rootScope.config || $rootScope.setupInProgress) {
				return true;
			}
			return $rootScope.needsWizard && $rootScope.needsWizard();
		}

		function syncWizardConfigInputsFromDom() {
			if (!isWizardConfigOverlayActive()) {
				return;
			}
			var prefix = '.fa-overlay-screen ';
			$rootScope.budget = readInputInt(prefix + 'input[ng-model="budget"]', $rootScope.budget);
			$rootScope.durataAstaDefault = readInputInt(prefix + 'input[ng-model="durataAstaDefault"]', $rootScope.durataAstaDefault);
			$rootScope.maxP = readInputInt(prefix + 'input[ng-model="maxP"]', $rootScope.maxP);
			$rootScope.maxD = readInputInt(prefix + 'input[ng-model="maxD"]', $rootScope.maxD);
			$rootScope.maxC = readInputInt(prefix + 'input[ng-model="maxC"]', $rootScope.maxC);
			$rootScope.maxA = readInputInt(prefix + 'input[ng-model="maxA"]', $rootScope.maxA);
		}

		function syncSettingsInputsFromDom() {
			if (!$rootScope.showSettings) {
				return;
			}
			syncWizardConfigInputsFromDom();
		}

		function syncSettingsDomFromScope() {
			if (!$rootScope.showSettings) {
				return;
			}
			var prefix = '.fa-overlay-screen ';
			var fields = {
				budget: $rootScope.budget,
				durataAstaDefault: $rootScope.durataAstaDefault,
				maxP: $rootScope.maxP,
				maxD: $rootScope.maxD,
				maxC: $rootScope.maxC,
				maxA: $rootScope.maxA
			};
			angular.forEach(fields, function(val, model) {
				var el = document.querySelector(prefix + 'input[ng-model="' + model + '"]');
				if (el) {
					el.value = val == null ? '' : val;
				}
			});
		}

		function coerceSettingsScopeValues() {
			$rootScope.budget = toSettingsInt($rootScope.budget, $rootScope.budget);
			$rootScope.durataAstaDefault = toSettingsInt($rootScope.durataAstaDefault, $rootScope.durataAstaDefault);
			$rootScope.maxP = toSettingsInt($rootScope.maxP, $rootScope.maxP);
			$rootScope.maxD = toSettingsInt($rootScope.maxD, $rootScope.maxD);
			$rootScope.maxC = toSettingsInt($rootScope.maxC, $rootScope.maxC);
			$rootScope.maxA = toSettingsInt($rootScope.maxA, $rootScope.maxA);
		}

		function coerceSettingsFormValues() {
			syncSettingsInputsFromDom();
			coerceSettingsScopeValues();
		}

		function applySettingsFromInit(data) {
			if (!data || data.DA_CONFIGURARE) {
				return;
			}
			if (data.isATurni === 'S') {
				$rootScope.isATurni = true;
			} else {
				$rootScope.isATurni = false;
			}
			if (data.isSingle === 'S') {
				$rootScope.isSingle = true;
			} else {
				$rootScope.isSingle = false;
			}
			if (data.isMantra === 'S') {
				$rootScope.isMantra = true;
			} else {
				$rootScope.isMantra = false;
			}
			$rootScope.budget = data.budget;
			if (data.durataAsta != null) {
				$rootScope.applyDurataAstaFromServer(data.durataAsta);
			}
			$rootScope.numAcquisti = data.numAcquisti;
			$rootScope.numMinAcquisti = data.numMinAcquisti;
			$rootScope.maxP = data.maxP;
			$rootScope.maxD = data.maxD;
			$rootScope.maxC = data.maxC;
			$rootScope.maxA = data.maxA;
			$rootScope.minP = data.minP;
			$rootScope.minD = data.minD;
			$rootScope.minC = data.minC;
			$rootScope.minA = data.minA;
			if (data.elencoAllenatori) {
				$rootScope.elencoAllenatori = data.elencoAllenatori;
				angular.forEach($rootScope.elencoAllenatori, function(u) {
					if (!u.nuovoNome) {
						u.nuovoNome = u.nome;
					}
				});
			}
			if (data.calciatori) {
				$rootScope.calciatori = data.calciatori;
			}
			if ($rootScope.calcolaIsAdmin) {
				$rootScope.calcolaIsAdmin();
			}
		}

		function reloadSettingsFromServer() {
			return $resource('./init', {}).get().$promise.then(function(data) {
				applySettingsFromInit(data);
				return data;
			});
		}

		function normalizeTeamSnapshot(teams) {
			return (teams || []).map(function(u) {
				return {
					id: u.id,
					nuovoNome: (u.nuovoNome || u.nome || '').trim(),
					isAdmin: !!u.isAdmin,
					ordine: u.ordine
				};
			}).sort(function(a, b) {
				return (a.id || 0) - (b.id || 0);
			});
		}

		function captureSettingsSnapshot(fromScopeOnly) {
			if (fromScopeOnly) {
				coerceSettingsScopeValues();
			} else {
				coerceSettingsFormValues();
			}
			if ($rootScope.isMantra) {
				$rootScope.maxP = $rootScope.maxA;
			} else {
				$rootScope.syncRosaMinMax();
			}
			return {
				budget: toSettingsInt($rootScope.budget, 0),
				durataAstaDefault: toSettingsInt($rootScope.durataAstaDefault, 0),
				isATurni: !!$rootScope.isATurni,
				isSingle: !!$rootScope.isSingle,
				isMantra: !!$rootScope.isMantra,
				maxP: toSettingsInt($rootScope.maxP, 0),
				maxD: toSettingsInt($rootScope.maxD, 0),
				maxC: toSettingsInt($rootScope.maxC, 0),
				maxA: toSettingsInt($rootScope.maxA, 0),
				teams: normalizeTeamSnapshot($rootScope.elencoAllenatori)
			};
		}

		function restoreSettingsSnapshot() {
			if (!settingsSnapshot) {
				return;
			}
			var s = settingsSnapshot;
			$rootScope.budget = s.budget;
			$rootScope.durataAstaDefault = s.durataAstaDefault;
			$rootScope.isATurni = s.isATurni;
			$rootScope.isSingle = s.isSingle;
			$rootScope.isMantra = s.isMantra;
			$rootScope.maxP = s.maxP;
			$rootScope.maxD = s.maxD;
			$rootScope.maxC = s.maxC;
			$rootScope.maxA = s.maxA;
			$rootScope.syncRosaMinMax && $rootScope.syncRosaMinMax();
			var byId = {};
			angular.forEach(s.teams, function(t) {
				byId[t.id] = t;
			});
			angular.forEach($rootScope.elencoAllenatori || [], function(u) {
				var t = byId[u.id];
				if (t) {
					u.nuovoNome = t.nuovoNome;
					u.isAdmin = t.isAdmin;
					u.ordine = t.ordine;
				}
			});
			$rootScope.syncDurataAstaFromConfig && $rootScope.syncDurataAstaFromConfig();
			syncSettingsDomFromScope();
		}

		function readSettingsState() {
			coerceSettingsFormValues();
			return {
				budget: toSettingsInt($rootScope.budget, 0),
				durataAstaDefault: toSettingsInt($rootScope.durataAstaDefault, 0),
				isATurni: !!$rootScope.isATurni,
				isSingle: !!$rootScope.isSingle,
				isMantra: !!$rootScope.isMantra,
				maxP: toSettingsInt($rootScope.maxP, 0),
				maxD: toSettingsInt($rootScope.maxD, 0),
				maxC: toSettingsInt($rootScope.maxC, 0),
				maxA: toSettingsInt($rootScope.maxA, 0),
				teams: normalizeTeamSnapshot($rootScope.elencoAllenatori)
			};
		}

		function isSettingsDirty() {
			if (!$rootScope.showSettings || !settingsSnapshot) {
				return false;
			}
			var cur = readSettingsState();
			var snap = settingsSnapshot;
			if (cur.budget !== snap.budget) return true;
			if (cur.durataAstaDefault !== snap.durataAstaDefault) return true;
			if (cur.isATurni !== snap.isATurni) return true;
			if (cur.isSingle !== snap.isSingle) return true;
			if (cur.isMantra !== snap.isMantra) return true;
			if (cur.maxP !== snap.maxP) return true;
			if (cur.maxD !== snap.maxD) return true;
			if (cur.maxC !== snap.maxC) return true;
			if (cur.maxA !== snap.maxA) return true;
			return JSON.stringify(cur.teams) !== JSON.stringify(snap.teams);
		}

		function prepareSettingsStepBeforeSave() {
			coerceSettingsFormValues();
			if ($rootScope.wizardStep === 2 || $rootScope.wizardStep >= SETTINGS_LAST_STEP) {
				$rootScope.syncDurataAstaFromConfig && $rootScope.syncDurataAstaFromConfig();
			}
			if ($rootScope.wizardStep === 3 || $rootScope.wizardStep >= 4) {
				if ($rootScope.isMantra) {
					$rootScope.maxP = $rootScope.maxA;
				} else {
					$rootScope.syncRosaMinMax && $rootScope.syncRosaMinMax();
				}
			}
		}

		function saveSettingsIfDirty() {
			prepareSettingsStepBeforeSave();
			if (!isSettingsDirty()) {
				return $q.when({ esitoDispositiva: 'OK', skipped: true });
			}
			return salvaImpostazioniLega();
		}

		var SETTINGS_STEPS = [2, 3, 4, 5];
		$rootScope.settingsSteps = function() {
			return SETTINGS_STEPS;
		};

		$rootScope.settingsStepTitle = function(step) {
			switch (step) {
				case 2: return 'Regole';
				case 3: return 'Rosa';
				case 4: return 'Squadre';
				case 5: return 'Quotazioni';
				default: return '';
			}
		};

		$rootScope.isSettingsDirty = function() {
			return isSettingsDirty();
		};

		$rootScope.settingsGoToStep = function(target) {
			if (!$rootScope.showSettings || $rootScope.wizardBusy || isSettingsDirty()) {
				return $q.when();
			}
			target = parseInt(target, 10);
			if (target < SETTINGS_FIRST_STEP || target > SETTINGS_LAST_STEP || target === $rootScope.wizardStep) {
				return $q.when();
			}
			$rootScope.wizardStep = target;
			return $q.when();
		};

		$rootScope.wizardPrev = function() {
			var minStep = 4;
			if ($rootScope.showSettings) {
				if (isSettingsDirty()) {
					return;
				}
				minStep = SETTINGS_FIRST_STEP;
			} else if ($rootScope.config || $rootScope.setupInProgress) {
				minStep = 1;
			}
			if ($rootScope.wizardStep > minStep) {
				$rootScope.wizardStep--;
				if ($rootScope.wizardSoloUpload && $rootScope.wizardSoloUpload() && $rootScope.wizardStep <= 4) {
					$rootScope.setupInProgress = true;
				}
			}
		};

		function finishWizardBusy() {
			$rootScope.wizardBusy = false;
			updateWizardFooterState();
		}

		$rootScope.wizardNext = function() {
			if ($rootScope.wizardBusy) return $q.when();
			if ($rootScope.showSettings) {
				if (isSettingsDirty() || $rootScope.wizardStep >= SETTINGS_LAST_STEP) {
					return $q.when();
				}
				$rootScope.wizardStep++;
				return $q.when();
			}
			if ($rootScope.wizardStep === 3 && $rootScope.config && !$rootScope.showSettings) {
				$rootScope.syncRosaMinMax();
				$rootScope.wizardBusy = true;
				return $rootScope.wizardCreateLega().finally(finishWizardBusy);
			}
			if ($rootScope.wizardStep === 4 && !$rootScope.showSettings) {
				$rootScope.wizardBusy = true;
				return $rootScope.wizardSaveUsers().finally(finishWizardBusy);
			}
			if ($rootScope.wizardStep < 5) {
				$rootScope.wizardStep++;
			}
			return $q.when();
		};

		$rootScope.closeUserMenu = function() {
			$rootScope.userMenuOpen = false;
		};

		$rootScope.toggleUserMenu = function($event) {
			if ($event && $event.stopPropagation) {
				$event.stopPropagation();
			}
			$rootScope.userMenuOpen = !$rootScope.userMenuOpen;
		};

		$rootScope.userMenuOpzioni = function() {
			$rootScope.closeUserMenu();
			if ($rootScope.isAdmin && $rootScope.openSettings) {
				$rootScope.openSettings();
			}
		};

		$rootScope.userMenuEsporta = function() {
			$rootScope.closeUserMenu();
			if (!$rootScope.isAdmin) {
				return;
			}
			window.location.href = $rootScope.isMantra ? './esportaMantra' : './esporta';
		};

		$rootScope.userMenuLogoutTutti = function() {
			$rootScope.closeUserMenu();
			if ($rootScope.isAdmin && $rootScope.disconnectAll) {
				$rootScope.disconnectAll();
			}
		};

		$rootScope.userMenuLogout = function() {
			$rootScope.closeUserMenu();
			if ($rootScope.forzaLogout) {
				$rootScope.forzaLogout();
			}
		};

		$rootScope.syncWizardConfigInputsFromDom = syncWizardConfigInputsFromDom;

		$rootScope.openSettings = function() {
			$rootScope.showSettings = true;
			$rootScope.wizardStep = SETTINGS_FIRST_STEP;
			$rootScope.wizardBusy = true;
			return reloadSettingsFromServer().then(function() {
				syncSettingsDomFromScope();
				settingsSnapshot = captureSettingsSnapshot(true);
				updateWizardFooterState();
			}).finally(function() {
				$rootScope.wizardBusy = false;
				updateWizardFooterState();
			});
		};

		function salvaImpostazioniLega() {
			if (!$rootScope.isAdmin || !$rootScope.aggiornaConfigLega) {
				alert('Solo l\'amministratore può salvare le impostazioni.');
				return $q.reject('not-admin');
			}
			prepareSettingsStepBeforeSave();
			$rootScope.syncDurataAstaFromConfig && $rootScope.syncDurataAstaFromConfig();
			if ($rootScope.isMantra) {
				$rootScope.maxP = $rootScope.maxA;
			} else {
				$rootScope.syncRosaMinMax();
			}
			return $rootScope.aggiornaConfigLega($rootScope.isAdmin, { stayOnPage: true }).then(function(data) {
				if (!data || data.esitoDispositiva !== 'OK') {
					alert('Salvataggio impostazioni fallito.');
					return $q.reject('save-failed');
				}
				if (data.durataAsta != null) {
					$rootScope.applyDurataAstaFromServer(data.durataAsta);
				}
				return reloadSettingsFromServer().then(function() {
					if ($rootScope.showSettings) {
						syncSettingsDomFromScope();
						settingsSnapshot = captureSettingsSnapshot(true);
						updateWizardFooterState();
					}
					return data;
				});
			});
		}

		$rootScope.salvaImpostazioniLega = salvaImpostazioniLega;

		$rootScope.closeSettingsPanel = function() {
			if (!$rootScope.showSettings || $rootScope.wizardBusy || isSettingsDirty()) {
				return $q.when();
			}
			$rootScope.showSettings = false;
			settingsSnapshot = null;
			return $q.when();
		};

		$rootScope.discardSettingsChanges = function() {
			if (!$rootScope.showSettings || $rootScope.wizardBusy || !isSettingsDirty()) {
				return $q.when();
			}
			restoreSettingsSnapshot();
			settingsSnapshot = captureSettingsSnapshot(true);
			updateWizardFooterState();
			return $q.when();
		};

		$rootScope.saveSettings = function() {
			if (!$rootScope.showSettings || $rootScope.wizardBusy || !isSettingsDirty()) {
				return $q.when();
			}
			$rootScope.wizardBusy = true;
			return salvaImpostazioniLega().then(function(data) {
				return data;
			}).finally(finishWizardBusy);
		};

		$rootScope.saveSettingsAndClose = function() {
			if (!$rootScope.showSettings || $rootScope.wizardBusy) {
				return $q.when();
			}
			if (!isSettingsDirty()) {
				return $rootScope.closeSettingsPanel();
			}
			$rootScope.wizardBusy = true;
			return salvaImpostazioniLega().then(function(data) {
				if (data && data.esitoDispositiva === 'OK') {
					$rootScope.showSettings = false;
					settingsSnapshot = null;
				}
				return data;
			}).finally(finishWizardBusy);
		};

		$rootScope.cancelSettings = $rootScope.closeSettingsPanel;
		$rootScope.closeSettings = $rootScope.closeSettingsPanel;

		$rootScope.setAdminUtente = function(utente) {
			angular.forEach($rootScope.elencoAllenatori || [], function(u) {
				u.isAdmin = (u.id === utente.id);
			});
		};

		function defaultAdminLoginNome() {
			var admin = $rootScope.trovaAllenatoreAdmin && $rootScope.trovaAllenatoreAdmin();
			if (admin) {
				return admin.nuovoNome || admin.nome || BOOTSTRAP_ADMIN_NOME;
			}
			return BOOTSTRAP_ADMIN_NOME;
		}

		$rootScope.entraComeAdminBootstrap = function() {
			if (!$rootScope.config) {
				return $q.when();
			}
			var nome = BOOTSTRAP_ADMIN_NOME;

			$rootScope.applyClassicDefaults && $rootScope.applyClassicDefaults();
			$rootScope.wizardUploadCount = null;
			if ($rootScope.markSetupTeamsSaved) {
				$rootScope.markSetupTeamsSaved(false);
			}
			if ($rootScope.markSetupComplete) {
				$rootScope.markSetupComplete(false);
			}
			$rootScope.setupInProgress = true;
			$rootScope.setupCompletato = false;
			$rootScope.adminLoginNome = nome;
			$rootScope.isAdminBootstrap = true;
			$rootScope.isAdmin = true;
			$rootScope.nomegiocatore = nome;
			$rootScope.idgiocatore = BOOTSTRAP_ADMIN_ID;
			return $rootScope.callDoConnect(nome, BOOTSTRAP_ADMIN_ID, '').then(function() {
				$rootScope.wizardStep = 1;
				syncWizardStepAfterInit();
			});
		};

		$rootScope.entraCome = function(utenteOrId) {
			if (utenteOrId == null) return $q.when();
			var targetId = (typeof utenteOrId === 'object') ? utenteOrId.id : utenteOrId;
			var connect = function() {
				var utente = trovaUtentePerId(targetId);
				if (!utente) {
					alert('Squadra non trovata. Ricarica la pagina.');
					return $q.when();
				}
				var nome = nomeAccesso(utente);
				if (!nome) {
					alert('Nome squadra non valido.');
					return $q.when();
				}
				if ($rootScope.isUtenteConnesso(nome)) {
					alert('Questa squadra è già connessa da un\'altra sessione.');
					return $q.when();
				}
				utente.nuovoNome = nome;
				return $rootScope.callDoConnect(nome, utente.id, utente.pwd || '').then(function() {
					if ($rootScope.utenti && $rootScope.utenti.indexOf(nome) === -1) {
						$rootScope.utenti.push(nome);
					}
					var afterRefresh = function() {
						if ($rootScope.calcolaIsAdmin) {
							$rootScope.calcolaIsAdmin();
						}
						syncWizardStepAfterInit();
					};
					if ($rootScope.refreshLoginElenco) {
						return $rootScope.refreshLoginElenco().then(afterRefresh);
					}
					afterRefresh();
					return $q.when();
				});
			};
			if ($rootScope.refreshLoginElenco) {
				return $rootScope.refreshLoginElenco().then(connect);
			}
			return connect();
		};

		function chiaviSquadra(utente) {
			if (!utente) return [];
			var keys = [];
			if (utente.nome) keys.push(utente.nome);
			if (utente.nuovoNome && utente.nuovoNome !== utente.nome) keys.push(utente.nuovoNome);
			return keys;
		}

		function isWsPronto() {
			return !!(window.FantaWsConnection && FantaWsConnection.isReady());
		}

		function trovaPingSquadra(utente) {
			if (!$rootScope.pingUtenti || !utente) return -1;
			var keys = chiaviSquadra(utente);
			if ($rootScope.isMiaSquadra(utente) && $rootScope.nomegiocatore) {
				keys.push($rootScope.nomegiocatore);
			}
			for (var i = 0; i < keys.length; i++) {
				var entry = $rootScope.pingUtenti[keys[i]];
				if (entry && entry.checkPing != null) {
					return entry.checkPing;
				}
			}
			return -1;
		}

		$rootScope.isUtenteConnesso = function(nome) {
			if (!$rootScope.utenti || !nome) return false;
			if ($rootScope.utenti.indexOf(nome) > -1) return true;
			var found = false;
			angular.forEach($rootScope.elencoAllenatori || [], function(u) {
				if ((u.nuovoNome || u.nome) === nome && $rootScope.utenti.indexOf(u.nome) > -1) {
					found = true;
				}
			});
			return found;
		};

		$rootScope.nomeUtenteAttivo = function() {
			if ($rootScope.nomegiocatore) return $rootScope.nomegiocatore;
			if (window.FantaWsConnection && FantaWsConnection.getSessionUser()) {
				return FantaWsConnection.getSessionUser();
			}
			return '';
		};

		$rootScope.isSquadraConnessa = function(utente) {
			if (!utente) return false;
			var attivo = $rootScope.nomeUtenteAttivo();
			if (attivo && $rootScope.isMiaSquadra(utente) && isWsPronto()) {
				return true;
			}
			if (!$rootScope.utenti) return false;
			var keys = chiaviSquadra(utente);
			for (var i = 0; i < keys.length; i++) {
				if ($rootScope.utenti.indexOf(keys[i]) > -1) return true;
			}
			return false;
		};

		$rootScope.pingSquadraMs = function(utente) {
			return trovaPingSquadra(utente);
		};

		$rootScope.qualitaConnessioneSquadra = function(utente) {
			if (!$rootScope.isSquadraConnessa(utente)) {
				return { label: 'Off', level: 'off' };
			}
			var ms = trovaPingSquadra(utente);
			var scaduta = false;
			angular.forEach(chiaviSquadra(utente), function(k) {
				if ($rootScope.utentiScaduti && $rootScope.utentiScaduti.indexOf(k) > -1) {
					scaduta = true;
				}
			});
			if (scaduta || (ms >= 0 && ms > 20000)) {
				return { label: 'Scaduta', level: 'bad' };
			}
			if (ms < 0) {
				return { label: 'OK', level: 'ok' };
			}
			if (ms > 8000) {
				return { label: 'Lenta', level: 'warn' };
			}
			if (ms > 3000) {
				return { label: 'Buona', level: 'ok' };
			}
			return { label: 'Ottima', level: 'great' };
		};

		$rootScope.teamStatusBadgeClass = function(utente) {
			// Stringa stabile: oggetti nuovi in ng-class a ogni digest → risk infdig
			var parts = [];
			if ($rootScope.isSquadraConnessa(utente)) {
				parts.push('is-online');
			}
			var q = $rootScope.qualitaConnessioneSquadra(utente);
			if (q && q.level && q.level !== 'off') {
				parts.push('is-' + q.level);
			}
			return parts.join(' ');
		};

		$rootScope.isMiaSquadra = function(utente) {
			if (!utente) return false;
			var attivo = $rootScope.nomeUtenteAttivo();
			if (!attivo) return false;
			var label = utente.nuovoNome || utente.nome;
			return label === attivo || utente.nome === attivo;
		};

		$rootScope.haIlTurnoUtente = function(utente) {
			if (!utente) return false;
			return $rootScope.haIlTurno(utente.nuovoNome || utente.nome);
		};

		$rootScope.acquistiPerSquadra = {};
		$rootScope.rosaSlotPerSquadra = {};
		$rootScope.acquistiRecentiIds = {};
		$rootScope.haAcquistoRecenteBadge = false;
		var EMPTY_ROSA_SLOTS = [];
		var adminElencoImpersonazioneCache = [];
		var ACQUISTO_RECENTE_MS = 120000;

		$rootScope.segnaAcquistoRecente = function(idGiocatore) {
			if (idGiocatore == null || idGiocatore === '') return;
			if (!$rootScope.acquistiRecentiIds) {
				$rootScope.acquistiRecentiIds = {};
			}
			$rootScope.acquistiRecentiIds[String(idGiocatore)] = Date.now();
			$rootScope.haAcquistoRecenteBadge = true;
		};

		$rootScope.segnaAcquistoRecenteDaOffertaCorrente = function() {
			var ov = $rootScope.offertaVincente;
			if (!ov || !ov.giocatore) return;
			var id = ov.giocatore.id != null ? ov.giocatore.id : ov.giocatore.idGiocatore;
			$rootScope.segnaAcquistoRecente(id);
		};

		$rootScope.isAcquistoRecente = function(g) {
			if (!g || !$rootScope.acquistiRecentiIds) return false;
			var id = g.idGiocatore != null ? g.idGiocatore : g.id;
			if (id == null || id === '') return false;
			var ts = $rootScope.acquistiRecentiIds[String(id)];
			if (!ts) return false;
			if ((Date.now() - ts) > ACQUISTO_RECENTE_MS) {
				delete $rootScope.acquistiRecentiIds[String(id)];
				return false;
			}
			return true;
		};

		$rootScope.consumaBadgeAcquistoTab = function() {
			$rootScope.haAcquistoRecenteBadge = false;
		};

		function buildAcquistiList(nomeAllenatore) {
			if (!$rootScope.giocatoriPerSquadra) return [];
			var value = $rootScope.giocatoriPerSquadra[nomeAllenatore];
			if (!value) {
				angular.forEach($rootScope.elencoAllenatori || [], function(u) {
					if ((u.nuovoNome === nomeAllenatore || u.nome === nomeAllenatore) && $rootScope.giocatoriPerSquadra[u.nome]) {
						value = $rootScope.giocatoriPerSquadra[u.nome];
					}
				});
			}
			if (!value) return [];
			var list = [];
			angular.forEach(value.ruoli || {}, function(giocatori, ruolo) {
				angular.forEach(giocatori || [], function(g) {
					list.push({
						ruolo: g.ruolo || ruolo,
						nome: g.giocatore,
						squadra: g.squadra,
						costo: g.costo,
						idGiocatore: g.idGiocatore,
						idAllenatore: g.idAllenatore,
						under23: g.under23,
						allenatore: nomeAllenatore
					});
				});
			});
			return list;
		}

		function rebuildAcquistiPerSquadra() {
			var cache = {};
			angular.forEach($rootScope.elencoAllenatori || [], function(u) {
				cache[u.nome] = buildAcquistiList(u.nome);
				if (u.nuovoNome && u.nuovoNome !== u.nome) {
					cache[u.nuovoNome] = cache[u.nome];
				}
			});
			angular.forEach($rootScope.giocatoriPerSquadra || {}, function(v, nome) {
				if (!cache[nome]) {
					cache[nome] = buildAcquistiList(nome);
				}
			});
			$rootScope.acquistiPerSquadra = cache;
			rebuildRosaSlotPerSquadra();
		}

		function buildRosaSlotsForUtente(utente) {
			if (!utente) return EMPTY_ROSA_SLOTS;
			var acquistati = $rootScope.getAcquistiSquadra(utente.nome);
			if (utente.nuovoNome && utente.nuovoNome !== utente.nome) {
				var alt = $rootScope.getAcquistiSquadra(utente.nuovoNome);
				if (alt.length > acquistati.length) {
					acquistati = alt;
				}
			}
			var byRole = { P: [], D: [], C: [], A: [] };
			angular.forEach(acquistati, function(g) {
				var r = String(g.ruolo || '').charAt(0).toUpperCase();
				if (byRole[r]) {
					byRole[r].push(g);
				}
			});
			var slots = [];
			function pushRuolo(ruolo, max) {
				var n = parseInt(max, 10) || 0;
				var pool = byRole[ruolo] || [];
				for (var i = 0; i < n; i++) {
					slots.push({ ruolo: ruolo, g: pool[i] || null });
				}
			}
			if ($rootScope.isMantra) {
				var tot = parseInt($rootScope.numAcquisti, 10) || 0;
				if (!tot) {
					tot = (parseInt($rootScope.maxP, 10) || 0)
						+ (parseInt($rootScope.maxD, 10) || 0)
						+ (parseInt($rootScope.maxC, 10) || 0)
						+ (parseInt($rootScope.maxA, 10) || 0);
				}
				var flat = acquistati.slice();
				for (var j = 0; j < tot; j++) {
					var g = flat[j] || null;
					slots.push({
						ruolo: g ? (String(g.ruolo || '').charAt(0).toUpperCase() || '·') : '·',
						g: g
					});
				}
			} else {
				pushRuolo('P', $rootScope.maxP);
				pushRuolo('D', $rootScope.maxD);
				pushRuolo('C', $rootScope.maxC);
				pushRuolo('A', $rootScope.maxA);
			}
			return slots;
		}

		function rebuildRosaSlotPerSquadra() {
			var cache = {};
			angular.forEach($rootScope.elencoAllenatori || [], function(u) {
				var slots = buildRosaSlotsForUtente(u);
				cache[u.nome] = slots;
				if (u.nuovoNome && u.nuovoNome !== u.nome) {
					cache[u.nuovoNome] = slots;
				}
			});
			$rootScope.rosaSlotPerSquadra = cache;
		}

		function rebuildAdminElencoImpersonazione() {
			var teams = ($rootScope.elencoAllenatori || []).slice();
			teams.sort(function(a, b) {
				var aSelf = String(a.id) === String($rootScope.idgiocatore) ? 0 : 1;
				var bSelf = String(b.id) === String($rootScope.idgiocatore) ? 0 : 1;
				if (aSelf !== bSelf) return aSelf - bSelf;
				return (a.ordine || 0) - (b.ordine || 0);
			});
			adminElencoImpersonazioneCache = teams;
		}

		$rootScope.$watch('giocatoriPerSquadra', rebuildAcquistiPerSquadra, true);
		$rootScope.$watch('elencoAllenatori', function(list) {
			ensureSlotDefaultNames();
			rebuildAcquistiPerSquadra();
			rebuildLoginUtentiOrdinati();
			if ($rootScope.isSetupLogin && $rootScope.isSetupLogin()) {
				$rootScope.adminLoginNome = defaultAdminLoginNome();
			}
		}, true);

		$rootScope.$watch('utenti', function() {
			rebuildLoginUtentiOrdinati();
		}, true);

		$rootScope.$watch(function() {
			return $rootScope.showLoginScreen && $rootScope.showLoginScreen();
		}, function(show) {
			if (show && $rootScope.isLegaCreata && $rootScope.isLegaCreata()) {
				$rootScope.refreshLoginElenco();
			}
		});

		$rootScope.getAcquistiSquadra = function(nomeAllenatore) {
			return ($rootScope.acquistiPerSquadra && $rootScope.acquistiPerSquadra[nomeAllenatore]) || [];
		};

		$rootScope.tagliaModal = null;
		$rootScope.tagliaBusy = false;

		$rootScope.apriDettaglioAcquisto = function(slot, utente, $event) {
			if ($event) {
				if ($event.stopPropagation) $event.stopPropagation();
				if ($event.preventDefault) $event.preventDefault();
			}
			if (!$rootScope.isAdmin || !slot || !slot.g || slot.g.idGiocatore == null) {
				return;
			}
			$rootScope.tagliaModal = {
				idGiocatore: slot.g.idGiocatore,
				idAllenatore: slot.g.idAllenatore != null ? slot.g.idAllenatore : utente.id,
				giocatore: slot.g.nome,
				ruolo: slot.g.ruolo || slot.ruolo,
				squadra: slot.g.squadra || '',
				costo: slot.g.costo,
				allenatore: $rootScope.nomeAllenatoreVisibile(utente) || utente.nome,
				under23: slot.g.under23
			};
		};

		$rootScope.chiudiTagliaModal = function() {
			if ($rootScope.tagliaBusy) return;
			$rootScope.tagliaModal = null;
		};

		$rootScope.confermaTagliaAcquisto = function() {
			var m = $rootScope.tagliaModal;
			if (!$rootScope.isAdmin || !m || m.idGiocatore == null || $rootScope.tagliaBusy) {
				return $q.when();
			}
			var offerta = {
				idGiocatore: m.idGiocatore,
				idAllenatore: m.idAllenatore,
				giocatore: m.giocatore,
				ruolo: m.ruolo,
				squadra: m.squadra,
				costo: m.costo,
				allenatore: m.allenatore
			};
			$rootScope.tagliaBusy = true;
			$rootScope.tokenDispositiva = Math.floor(Math.random() * 10000 + 1);
			return $resource('./cancellaOfferta', {}).save({
				offerta: offerta,
				idgiocatore: $rootScope.idgiocatore,
				tokenDispositiva: $rootScope.tokenDispositiva
			}).$promise.then(function(data) {
				$rootScope.tagliaBusy = false;
				if (data.esitoDispositiva === 'OK') {
					if (data.ret) {
						$rootScope.cronologiaOfferte = data.ret;
					}
					$rootScope.tagliaModal = null;
					return data;
				}
				alert('Taglio non riuscito' + (data.errore ? ': ' + data.errore : '.'));
				return data;
			}, function(err) {
				$rootScope.tagliaBusy = false;
				var msg = (err && err.data && err.data.errore) ? err.data.errore : '';
				alert('Taglio non riuscito' + (msg ? ': ' + msg : '.'));
				return $q.reject(err);
			});
		};

		$rootScope.rosaSlotSquadra = function(utente) {
			if (!utente) return EMPTY_ROSA_SLOTS;
			var cache = $rootScope.rosaSlotPerSquadra || {};
			return cache[utente.nome] || cache[utente.nuovoNome] || EMPTY_ROSA_SLOTS;
		};

		$rootScope.teamsSearchQ = '';
		$rootScope.teamsSearchMatches = [];
		$rootScope.teamsSearchIndex = -1;

		$rootScope.teamsSearchTeamKey = function(utente) {
			if (!utente) return '';
			return 't-' + String(utente.id != null ? utente.id : utente.nome);
		};

		$rootScope.teamsSearchPlayerKey = function(utente, slotIndex) {
			if (!utente) return '';
			return 'p-' + String(utente.id != null ? utente.id : utente.nome) + '-' + slotIndex;
		};

		function teamsSearchNorm(s) {
			return String(s || '').toLowerCase().trim();
		}

		function rebuildTeamsSearchMatches() {
			var q = teamsSearchNorm($rootScope.teamsSearchQ);
			var matches = [];
			if (!q) {
				$rootScope.teamsSearchMatches = matches;
				$rootScope.teamsSearchIndex = -1;
				return;
			}
			var teams = ($rootScope.elencoAllenatori || []).slice().sort(function(a, b) {
				return (a.ordine || 0) - (b.ordine || 0);
			});
			angular.forEach(teams, function(u) {
				var teamLabel = $rootScope.nomeAllenatoreVisibile(u);
				if (teamsSearchNorm(teamLabel).indexOf(q) >= 0
						|| teamsSearchNorm(u.nome).indexOf(q) >= 0
						|| teamsSearchNorm(u.nuovoNome).indexOf(q) >= 0) {
					matches.push({ key: $rootScope.teamsSearchTeamKey(u), type: 'team' });
				}
				var slots = $rootScope.rosaSlotSquadra(u) || [];
				for (var i = 0; i < slots.length; i++) {
					var g = slots[i] && slots[i].g;
					if (!g) continue;
					var playerName = g.nome || g.giocatore || '';
					if (playerName && teamsSearchNorm(playerName).indexOf(q) >= 0) {
						matches.push({
							key: $rootScope.teamsSearchPlayerKey(u, i),
							type: 'player'
						});
					}
				}
			});
			$rootScope.teamsSearchMatches = matches;
			if (!matches.length) {
				$rootScope.teamsSearchIndex = -1;
			} else if ($rootScope.teamsSearchIndex < 0 || $rootScope.teamsSearchIndex >= matches.length) {
				$rootScope.teamsSearchIndex = 0;
			}
		}

		function scrollToTeamsSearchHit() {
			var matches = $rootScope.teamsSearchMatches || [];
			var idx = $rootScope.teamsSearchIndex;
			if (idx < 0 || idx >= matches.length) return;
			var key = matches[idx].key;
			$timeout(function() {
				var el = document.querySelector('[data-teams-hit="' + key + '"]');
				if (!el) return;
				var scroller = document.getElementById('fa-teams-scroll');
				if (scroller && scroller.contains(el)) {
					var scRect = scroller.getBoundingClientRect();
					var elRect = el.getBoundingClientRect();
					var top = scroller.scrollTop + (elRect.top - scRect.top)
						- (scRect.height / 2) + (elRect.height / 2);
					scroller.scrollTo({ top: Math.max(0, top), behavior: 'smooth' });
				} else if (el.scrollIntoView) {
					el.scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'nearest' });
				}
			}, 30);
		}

		$rootScope.isTeamsSearchHit = function(key) {
			if (!key || !$rootScope.teamsSearchQ) return false;
			var matches = $rootScope.teamsSearchMatches || [];
			for (var i = 0; i < matches.length; i++) {
				if (matches[i].key === key) return true;
			}
			return false;
		};

		$rootScope.isTeamsSearchActive = function(key) {
			if (!key || $rootScope.teamsSearchIndex < 0) return false;
			var m = $rootScope.teamsSearchMatches[$rootScope.teamsSearchIndex];
			return !!(m && m.key === key);
		};

		$rootScope.onTeamsSearchInput = function() {
			rebuildTeamsSearchMatches();
			if ($rootScope.teamsSearchMatches.length) {
				$rootScope.teamsSearchIndex = 0;
				scrollToTeamsSearchHit();
			}
		};

		$rootScope.clearTeamsSearch = function() {
			$rootScope.teamsSearchQ = '';
			$rootScope.teamsSearchMatches = [];
			$rootScope.teamsSearchIndex = -1;
		};

		$rootScope.teamsSearchNext = function() {
			var n = ($rootScope.teamsSearchMatches || []).length;
			if (!n) return;
			$rootScope.teamsSearchIndex = ($rootScope.teamsSearchIndex + 1) % n;
			scrollToTeamsSearchHit();
		};

		$rootScope.teamsSearchPrev = function() {
			var n = ($rootScope.teamsSearchMatches || []).length;
			if (!n) return;
			$rootScope.teamsSearchIndex = ($rootScope.teamsSearchIndex - 1 + n) % n;
			scrollToTeamsSearchHit();
		};

		$rootScope.onTeamsSearchKeydown = function($event) {
			if (!$event) return;
			if ($event.key === 'Enter' || $event.keyCode === 13) {
				$event.preventDefault();
				if ($event.shiftKey) {
					$rootScope.teamsSearchPrev();
				} else {
					$rootScope.teamsSearchNext();
				}
			} else if ($event.key === 'Escape' || $event.keyCode === 27) {
				$rootScope.clearTeamsSearch();
			}
		};

		var prevUiTabForTeamsSearch = $rootScope.uiTab;
		$rootScope.$watch('uiTab', function(tab) {
			if (prevUiTabForTeamsSearch === 'teams' && tab !== 'teams') {
				$rootScope.clearTeamsSearch();
			}
			prevUiTabForTeamsSearch = tab;
		});

		$rootScope.togglePlayerSort = function(field) {
			if ($rootScope.playerSortField === field) {
				$rootScope.playerSortRev = !$rootScope.playerSortRev;
			} else {
				$rootScope.playerSortField = field;
				$rootScope.playerSortRev = field === 'quotazione';
			}
		};

		$rootScope.togglePlayerSortDir = function() {
			$rootScope.playerSortRev = !$rootScope.playerSortRev;
		};

		$rootScope.haFiltriGiocatoriAttivi = function() {
			return !!(
				($rootScope.filterNome && String($rootScope.filterNome).trim())
				|| $rootScope.filterRuolo
				|| $rootScope.filterSquadra
				|| $rootScope.filterPreferito
				|| $rootScope.playerSortField !== 'quotazione'
				|| $rootScope.playerSortRev !== true
			);
		};

		$rootScope.resetFiltriGiocatori = function() {
			$rootScope.filterNome = '';
			$rootScope.filterRuolo = '';
			$rootScope.filterSquadra = '';
			$rootScope.filterPreferito = false;
			$rootScope.playerSortField = 'quotazione';
			$rootScope.playerSortRev = true;
		};

		$rootScope.nomeAllenatoreVisibile = function(utente) {
			return (utente && (utente.nuovoNome || utente.nome)) || '';
		};

		function nomeOperaComeDaUtente(utente) {
			if (!utente) return '';
			// avviabili / puoAvviareAsta usano il nome persistito in DB
			return (utente.nome || utente.nuovoNome || '').trim();
		}

		$rootScope.ensureAdminImpersonazione = function() {
			if (!$rootScope.isAdmin || $rootScope.idgiocatore == null || $rootScope.idgiocatore === '') {
				return;
			}
			var teams = $rootScope.elencoAllenatori || [];
			if (!teams.length) {
				return;
			}
			var selected = null;
			angular.forEach(teams, function(u) {
				if (String(u.id) === String($rootScope.idgiocatoreOperaCome)) {
					selected = u;
				}
			});
			if (!selected) {
				angular.forEach(teams, function(u) {
					if (String(u.id) === String($rootScope.idgiocatore)) {
						selected = u;
					}
				});
			}
			if (selected) {
				$rootScope.idgiocatoreOperaCome = selected.id;
				$rootScope.nomegiocatoreOperaCome = nomeOperaComeDaUtente(selected);
			}
		};

		$rootScope.adminElencoImpersonazione = function() {
			return adminElencoImpersonazioneCache;
		};

		$rootScope.adminPuoAvviareComeSelezionato = function() {
			if (!$rootScope.isAdmin || $rootScope.faseAsta !== 'IDLE' || !$rootScope.selCalciatoreId || !$rootScope.durataAsta) {
				return false;
			}
			if (!$rootScope.calciatoreAncoraDisponibile($rootScope.selCalciatoreId)) {
				return false;
			}
			var nome = $rootScope.astaPuntaNome();
			return !!nome && $rootScope.puoAvviareAsta(nome);
		};

		$rootScope.isAllenatoreDiTurno = function(utente) {
			if (!$rootScope.isATurni || !$rootScope.nomeGiocatoreTurno || !utente) {
				return false;
			}
			var turn = $rootScope.nomeGiocatoreTurno;
			return utente.nome === turn
				|| utente.nuovoNome === turn
				|| $rootScope.nomeAllenatoreVisibile(utente) === turn;
		};

		$rootScope.adminInAttesaTurnoComeAltri = function() {
			if (!$rootScope.isAdmin || $rootScope.faseAsta !== 'IDLE' || !$rootScope.selCalciatoreId) {
				return false;
			}
			if ($rootScope.adminPuoAvviareComeSelezionato()) {
				return false;
			}
			if (!$rootScope.isATurni || !$rootScope.nomeGiocatoreTurno) {
				return false;
			}
			return $rootScope.astaPuntaNome() !== $rootScope.nomeGiocatoreTurno;
		};

		$rootScope.adminHintIdleCentrale = function() {
			if (!$rootScope.isAdmin || !$rootScope.selCalciatoreId || $rootScope.adminPuoAvviareComeSelezionato()) {
				return '';
			}
			if ($rootScope.adminInAttesaTurnoComeAltri()) {
				return 'In attesa del turno di ' + $rootScope.nomeGiocatoreTurno;
			}
			var label = $rootScope.astaPuntaLabel() || 'Questa squadra';
			return label + ' non può acquistare questo giocatore';
		};

		$rootScope.adminAvviaComeSelezionato = function() {
			if (!$rootScope.adminPuoAvviareComeSelezionato()) {
				return;
			}
			var nome = $rootScope.astaPuntaNome();
			var id = $rootScope.astaPuntaId();
			if (nome && id != null) {
				$rootScope.inizia(nome, id);
			}
		};

		$rootScope.puoAvviareAstaCorrente = function() {
			if ($rootScope.isAdmin) {
				return $rootScope.adminPuoAvviareComeSelezionato()
					&& String($rootScope.astaPuntaId()) === String($rootScope.idgiocatore);
			}
			if ($rootScope.faseAsta !== 'IDLE' || !$rootScope.selCalciatoreId || !$rootScope.durataAsta) {
				return false;
			}
			if (!$rootScope.calciatoreAncoraDisponibile($rootScope.selCalciatoreId)) {
				return false;
			}
			var nome = $rootScope.nomeUtenteAttivo();
			if (!nome) return false;
			if ($rootScope.isATurni && $rootScope.nomeGiocatoreTurno !== nome) return false;
			return $rootScope.puoAvviareAsta(nome);
		};

		/** Detentore del turno (o tutti se non a turni) può aprire la selezione.
		 *  Admin: può sempre selezionare; Opera come influenza solo Avvia asta. */
		$rootScope.puoSelezionareGiocatoreIdle = function() {
			if (!$rootScope.nomeUtenteAttivo || !$rootScope.nomeUtenteAttivo()) {
				return false;
			}
			if (!$rootScope.isATurni) {
				return true;
			}
			if ($rootScope.isAdmin) {
				return true;
			}
			var me = $rootScope.nomeUtenteAttivo();
			if ($rootScope.nomeGiocatoreTurno && me && $rootScope.nomeGiocatoreTurno === me) {
				return true;
			}
			if ($rootScope.haIlTurno && $rootScope.nomegiocatore && $rootScope.haIlTurno($rootScope.nomegiocatore)) {
				return true;
			}
			return false;
		};

		/** Fine asta / cambio turno / cambio Opera come: niente nome giocatore se chi opera non può fare l'asta. */
		$rootScope.pulisciSelezioneSeNonDiTurno = function() {
			if (!$rootScope.selCalciatoreId) {
				return;
			}
			if ($rootScope.faseAsta === 'BIDDING' || $rootScope.faseAsta === 'DA_CONFERMARE') {
				return;
			}
			if ($rootScope.timeout) {
				return;
			}
			if ($rootScope.puoSelezionareGiocatoreIdle && $rootScope.puoSelezionareGiocatoreIdle()) {
				return;
			}
			if ($rootScope.deselezionaCalciatore) {
				$rootScope.deselezionaCalciatore();
			}
		};

		$rootScope.puoAvviareAstaPerTurno = function() {
			// Legacy: sostituito da adminPuoAvviareComeSelezionato per l'admin
			return false;
		};

		$rootScope.puoMostrareGavel = function(calciatore) {
			if (!calciatore || $rootScope.faseAsta !== 'IDLE') return false;
			if ($rootScope.selCalciatoreId !== calciatore.id) return false;
			if ($rootScope.isAdmin) {
				return $rootScope.adminPuoAvviareComeSelezionato();
			}
			return $rootScope.puoAvviareAstaCorrente();
		};

		$rootScope.avviaAstaCorrente = function() {
			if ($rootScope.isAdmin) {
				$rootScope.adminAvviaComeSelezionato();
				return;
			}
			var nome = $rootScope.nomeUtenteAttivo();
			var id = $rootScope.idgiocatore;
			if (nome && id != null) {
				$rootScope.inizia(nome, id);
			}
		};

		$rootScope.auctionTimerMaxMs = function() {
			return ($rootScope.durataAsta || 0) * 1000;
		};

		$rootScope.auctionTimerRemainingMs = function() {
			var max = $rootScope.auctionTimerMaxMs();
			var cur = $rootScope.contaTempo || 0;
			return Math.max(0, max - cur);
		};

		$rootScope.auctionTimerSeconds = function() {
			return Math.ceil($rootScope.auctionTimerRemainingMs() / 1000);
		};

		$rootScope.auctionTimerRingPct = function() {
			var max = $rootScope.auctionTimerMaxMs();
			if (!max) return 0;
			return ($rootScope.auctionTimerRemainingMs() / max) * 100;
		};

		$rootScope.auctionTimerRingClass = function() {
			var ts = $rootScope.timeStart;
			if (ts === 3) return 'is-danger';
			if (ts === 2) return 'is-warn';
			return 'is-ok';
		};

		$rootScope.auctionLeaderLabel = function() {
			if (!$rootScope.offertaVincente || !$rootScope.offertaVincente.nomegiocatore) {
				return '';
			}
			var id = $rootScope.offertaVincente.idgiocatore;
			var label = '';
			angular.forEach($rootScope.elencoAllenatori || [], function(a) {
				if (a.id === id || String(a.id) === String(id)) {
					label = a.nuovoNome || a.nome;
				}
			});
			return label || $rootScope.offertaVincente.nomegiocatore;
		};

		$rootScope.isUtenteVincendoAsta = function() {
			if (!$rootScope.offertaVincente || !$rootScope.offertaVincente.nomegiocatore) {
				return false;
			}
			var leader = $rootScope.offertaVincente.nomegiocatore;
			var leaderId = $rootScope.offertaVincente.idgiocatore;
			// Solo la propria squadra (non “Opera come”): evita «Stai vincendo» sull’altro utente
			if ($rootScope.nomegiocatore && leader === $rootScope.nomegiocatore) {
				return true;
			}
			if ($rootScope.idgiocatore != null && leaderId != null
					&& String($rootScope.idgiocatore) === String(leaderId)) {
				return true;
			}
			return false;
		};

		$rootScope.auctionWinnerBannerText = function() {
			if (!$rootScope.offertaVincente || !$rootScope.offertaVincente.giocatore) {
				return '';
			}
			var g = $rootScope.offertaVincente.giocatore;
			var label = $rootScope.auctionLeaderLabel();
			var offer = $rootScope.offertaVincente.offerta;
			if ($rootScope.isUtenteVincendoAsta()) {
				return label + ' Stai vincendo l\'asta!';
			}
			return label + ' sta vincendo ' + g.nome + ' (' + g.ruolo + ') ' + g.squadra + ' con ' + offer;
		};

		$rootScope.auctionConfirmBannerText = function() {
			if (!$rootScope.offertaVincente || !$rootScope.offertaVincente.giocatore) {
				return '';
			}
			var g = $rootScope.offertaVincente.giocatore;
			var label = $rootScope.auctionLeaderLabel();
			var offer = $rootScope.offertaVincente.offerta;
			return label + ' ha vinto ' + g.nome + ' con ' + offer + ' crediti';
		};

		$rootScope.normalizzaIdAllenatore = function(id) {
			if (id === '' || id === null || id === undefined) return null;
			var n = parseInt(id, 10);
			return isNaN(n) ? null : n;
		};

		$rootScope.astaPuntaNome = function() {
			if ($rootScope.isAdmin && $rootScope.idgiocatoreOperaCome >= 0 && $rootScope.nomegiocatoreOperaCome) {
				return $rootScope.nomegiocatoreOperaCome;
			}
			return $rootScope.nomegiocatore;
		};

		$rootScope.astaPuntaId = function() {
			if ($rootScope.isAdmin && $rootScope.idgiocatoreOperaCome >= 0) {
				return $rootScope.idgiocatoreOperaCome;
			}
			return $rootScope.idgiocatore;
		};

		$rootScope.astaPuntaLabel = function() {
			var id = $rootScope.astaPuntaId();
			if (id == null) return '';
			var label = '';
			angular.forEach($rootScope.elencoAllenatori || [], function(a) {
				if (String(a.id) === String(id)) label = a.nuovoNome || a.nome;
			});
			return label || $rootScope.astaPuntaNome();
		};

		$rootScope.selezionaSquadraPunta = function(allenatore) {
			if (!allenatore || !$rootScope.isAdmin) return;
			$rootScope.idgiocatoreOperaCome = allenatore.id;
			$rootScope.nomegiocatoreOperaCome = nomeOperaComeDaUtente(allenatore);
			if ($rootScope.pulisciSelezioneSeNonDiTurno) {
				$rootScope.pulisciSelezioneSeNonDiTurno();
			}
		};

		$rootScope.isSquadraPuntaAttiva = function(allenatore) {
			if (!allenatore) return false;
			var activeId = $rootScope.astaPuntaId();
			return String(allenatore.id) === String(activeId);
		};

		$rootScope.astaPuoRilanciare = function() {
			var nome = $rootScope.astaPuntaNome();
			if (!nome || !$rootScope.offertaVincente) return false;
			if ($rootScope.isSingle) return false;
			if ($rootScope.avviabili.indexOf(nome) < 0) return false;
			// Se sei già in testa non puoi (né devi) rilanciare
			if ($rootScope.offertaVincente.nomegiocatore === nome) return false;
			var puntaId = $rootScope.astaPuntaId && $rootScope.astaPuntaId();
			if (puntaId != null && $rootScope.offertaVincente.idgiocatore != null
					&& String(puntaId) === String($rootScope.offertaVincente.idgiocatore)) {
				return false;
			}
			return $rootScope.offertaVincente.offerta < $rootScope.getFromMapSpesoTotale('MAXRILANCIO', nome);
		};

		/** Minimo rilancio valido = offerta live + 1 */
		$rootScope.astaMinOfferta = function() {
			var cur = ($rootScope.offertaVincente && $rootScope.offertaVincente.offerta != null)
				? Number($rootScope.offertaVincente.offerta) : 0;
			if (isNaN(cur) || cur < 0) cur = 0;
			return cur + 1;
		};

		$rootScope.offertaPrivTroppoBassa = function() {
			if ($rootScope.offertaPriv == null || $rootScope.offertaPriv === '') return false;
			var v = Number($rootScope.offertaPriv);
			return !isNaN(v) && v < $rootScope.astaMinOfferta();
		};

		$rootScope.isOffertaPrivEditing = function() {
			return !!$rootScope.offertaPrivEditing;
		};

		/** Legge il valore reale dal DOM (su iOS ng-model resta spesso stale finché non blur) */
		$rootScope.leggiOffertaPrivInput = function() {
			var el = document.querySelector('.fa-bid-strip__input');
			if (el && el.value !== '' && el.value != null) {
				return el.value;
			}
			if ($rootScope._offertaPrivPendingSend != null && $rootScope._offertaPrivPendingSend !== '') {
				return $rootScope._offertaPrivPendingSend;
			}
			return $rootScope.offertaPriv;
		};

		$rootScope.canInviaOffertaPriv = function() {
			var raw = $rootScope.leggiOffertaPrivInput();
			if (raw == null || raw === '') return false;
			var v = Number(raw);
			if (isNaN(v) || v < $rootScope.astaMinOfferta()) return false;
			var nome = $rootScope.astaPuntaNome && $rootScope.astaPuntaNome();
			if (!nome || !$rootScope.testRilancia || !$rootScope.testRilancia(nome, v)) return false;
			var max = $rootScope.getFromMapSpesoTotale
				? Number($rootScope.getFromMapSpesoTotale('MAXRILANCIO', nome)) : null;
			if (max != null && !isNaN(max) && v > max) return false;
			return true;
		};

		/** Svuota il campo al focus (mobile) e nasconde i tasti rilancio finché digiti */
		$rootScope.onOffertaPrivFocus = function($event) {
			$rootScope.offertaPrivEditing = true;
			$rootScope._skipOffertaPrivBlur = false;
			$rootScope._offertaPrivPendingSend = null;
			if ($rootScope.isMobilePortraitUi && $rootScope.isMobilePortraitUi()) {
				$rootScope._offertaPrivPrimaFocus = $rootScope.offertaPriv;
				$rootScope.offertaPriv = '';
				var el = $event && $event.target;
				if (el) el.value = '';
			}
		};

		/**
		 * Invia offerta da tastiera/bottone.
		 * Su iOS il valore va letto dal DOM: ng-model spesso non è aggiornato.
		 */
		$rootScope.inviaOffertaPrivDaTastiera = function($event) {
			if ($event) {
				$event.preventDefault();
				if ($event.stopPropagation) $event.stopPropagation();
			}
			var min = $rootScope.astaMinOfferta();
			var nome = $rootScope.astaPuntaNome && $rootScope.astaPuntaNome();
			var max = nome && $rootScope.getFromMapSpesoTotale
				? Number($rootScope.getFromMapSpesoTotale('MAXRILANCIO', nome)) : null;
			var raw = $rootScope.leggiOffertaPrivInput();
			if (raw == null || raw === '') {
				$rootScope.offertaPrivEditing = true;
				return false;
			}
			var v = Number(String(raw).replace(',', '.').replace(/[^\d.-]/g, ''));
			if (isNaN(v) || v < min) {
				$rootScope.offertaPriv = min;
				$rootScope.offertaPrivEditing = true;
				return false;
			}
			if (max != null && !isNaN(max) && v > max) {
				v = max;
			} else {
				v = Math.floor(v);
			}
			if (!nome || !$rootScope.testRilancia || !$rootScope.testRilancia(nome, v)) {
				$rootScope.offertaPriv = v;
				$rootScope.offertaPrivEditing = true;
				return false;
			}
			$rootScope.offertaPriv = v;
			$rootScope.offertaPrivEditing = false;
			$rootScope._offertaPrivPendingSend = null;
			$rootScope.inviaOffertaLibera(nome, $rootScope.astaPuntaId(), v);
			try {
				var inp = document.querySelector('.fa-bid-strip__input');
				if (inp && inp.blur) inp.blur();
			} catch (e) {}
			return false;
		};

		$rootScope.onOffertaPrivKeydown = function($event) {
			if (!$event) return;
			var isEnter = $event.key === 'Enter' || $event.keyCode === 13 || $event.which === 13;
			if (!isEnter) return;
			$rootScope.inviaOffertaPrivDaTastiera($event);
		};

		/**
		 * Bottone Invia: cattura subito il valore (prima del blur iOS),
		 * poi invia. Niente preventDefault su touchstart (su iOS annulla il click).
		 */
		$rootScope.onOffertaPrivInviaTouch = function($event) {
			if ($rootScope._offertaPrivInvioLock) return;
			$rootScope._offertaPrivInvioLock = true;
			setTimeout(function() { $rootScope._offertaPrivInvioLock = false; }, 700);

			$rootScope._skipOffertaPrivBlur = true;
			var el = document.querySelector('.fa-bid-strip__input');
			$rootScope._offertaPrivPendingSend = (el && el.value != null) ? el.value : $rootScope.offertaPriv;

			if ($event) {
				$event.preventDefault();
				if ($event.stopPropagation) $event.stopPropagation();
			}
			$rootScope.inviaOffertaPrivDaTastiera($event);
		};

		/** Blur ritardato: su iOS arriva PRIMA del click sul bottone Invia */
		$rootScope.onOffertaPrivBlur = function() {
			setTimeout(function() {
				var applyBlur = function() {
					if ($rootScope._skipOffertaPrivBlur || $rootScope._offertaPrivInvioLock) {
						$rootScope._skipOffertaPrivBlur = false;
						return;
					}
					if (!$rootScope.offertaPrivEditing) return;
					var min = $rootScope.astaMinOfferta();
					var maxNome = $rootScope.astaPuntaNome && $rootScope.astaPuntaNome();
					var max = maxNome && $rootScope.getFromMapSpesoTotale
						? Number($rootScope.getFromMapSpesoTotale('MAXRILANCIO', maxNome)) : null;
					var raw = $rootScope.leggiOffertaPrivInput();
					if (raw == null || raw === '') {
						$rootScope.offertaPriv = min;
					} else {
						var v = Number(String(raw).replace(',', '.').replace(/[^\d.-]/g, ''));
						if (isNaN(v) || v < min) {
							$rootScope.offertaPriv = min;
						} else if (max != null && !isNaN(max) && v > max) {
							$rootScope.offertaPriv = max;
						} else {
							$rootScope.offertaPriv = Math.floor(v);
						}
					}
					$rootScope.offertaPrivEditing = false;
					$rootScope._offertaPrivPendingSend = null;
				};
				if (!$rootScope.$$phase) {
					$rootScope.$applyAsync(applyBlur);
				} else {
					applyBlur();
				}
			}, 250);
		};

		$rootScope.apriAssegna = function() {
			if ($rootScope.faseAsta === 'DA_CONFERMARE') {
				return $rootScope.apriForza();
			}
			if ($rootScope.faseAsta !== 'IDLE' || ($rootScope.isAstaInPausa && $rootScope.isAstaInPausa())) {
				return;
			}
			if (!$rootScope.selCalciatoreId) {
				// Niente popup: vai a Giocatori e apri Assegna dopo la scelta
				$rootScope.pendingAssegna = true;
				if ($rootScope.setUiTab) {
					$rootScope.setUiTab('players');
				}
				return;
			}
			$rootScope.pendingAssegna = false;
			var quot = parseInt($rootScope.selCalciatoreQuotazione, 10);
			$rootScope.forzaOfferta = quot > 0 ? quot : 1;
			$rootScope.forzaAllenatore = $rootScope.normalizzaIdAllenatore($rootScope.astaPuntaId());
			if ($rootScope.forzaAllenatore == null && $rootScope.elencoAllenatori && $rootScope.elencoAllenatori.length) {
				$rootScope.forzaAllenatore = $rootScope.normalizzaIdAllenatore($rootScope.elencoAllenatori[0].id);
			}
			$rootScope.abilitaForza = true;
		};

		$rootScope.apriForza = function() {
			if (!$rootScope.offertaVincente) return;
			$rootScope.forzaOfferta = $rootScope.offertaVincente.offerta;
			$rootScope.forzaAllenatore = $rootScope.normalizzaIdAllenatore($rootScope.offertaVincente.idgiocatore);
			$rootScope.abilitaForza = true;
		};

		$rootScope.chiudiForza = function() {
			$rootScope.abilitaForza = false;
			$rootScope.pendingAssegna = false;
		};

		$rootScope.confermaAssegna = function() {
			if ($rootScope.faseAsta === 'DA_CONFERMARE') {
				return $rootScope.forza(true);
			}
			if ($rootScope.faseAsta !== 'IDLE' || !$rootScope.selCalciatoreId) {
				alert('Seleziona un giocatore da assegnare');
				return $q.when();
			}
			var allenatoreId = $rootScope.forzaAllenatore;
			if (allenatoreId === '' || allenatoreId === null || allenatoreId === undefined) {
				alert('Seleziona la squadra');
				return $q.when();
			}
			var costo = parseInt($rootScope.forzaOfferta, 10);
			if (!costo || costo < 1) {
				alert('Inserisci un costo valido');
				return $q.when();
			}
			$rootScope.tokenDispositiva = Math.floor(Math.random() * 10000 + 1);
			return $resource('./assegnaGiocatore', {}).save({
				idCalciatore: $rootScope.selCalciatoreId,
				idAllenatore: allenatoreId,
				costo: costo,
				idgiocatore: $rootScope.idgiocatore,
				tokenDispositiva: $rootScope.tokenDispositiva
			}).$promise.then(function(data) {
				if (data.esitoDispositiva === 'OK') {
					if ($rootScope.segnaAcquistoRecente) {
						$rootScope.segnaAcquistoRecente($rootScope.selCalciatoreId);
					}
					$rootScope.abilitaForza = false;
					$rootScope.selCalciatoreId = null;
					$rootScope.selCalciatore = null;
					$rootScope.selCalciatoreNome = '';
					return data;
				}
				alert('Assegnazione non riuscita' + (data.errore ? ': ' + data.errore : '.'));
				return data;
			}, function(err) {
				var msg = (err && err.data && err.data.errore) ? err.data.errore : '';
				alert('Assegnazione non riuscita' + (msg ? ': ' + msg : '.'));
				return $q.reject(err);
			});
		};

		$rootScope.avviaCalciatoreAsta = function(calciatore, $event) {
			if ($event && $event.stopPropagation) $event.stopPropagation();
			$rootScope.selezionaCalciatore(calciatore);
			if ($rootScope.isAdmin && $rootScope.adminPuoAvviareComeSelezionato()) {
				$rootScope.adminAvviaComeSelezionato();
			} else if ($rootScope.puoAvviareAstaCorrente()) {
				$rootScope.avviaAstaCorrente();
			}
		};

		$rootScope.$watchGroup(['isAdmin', 'idgiocatore', 'elencoAllenatori'], function() {
			rebuildAdminElencoImpersonazione();
			if ($rootScope.isAdmin) {
				$rootScope.ensureAdminImpersonazione();
			}
		});

		['maxP', 'maxD', 'maxC', 'maxA', 'isMantra', 'numAcquisti'].forEach(function(field) {
			$rootScope.$watch(field, rebuildRosaSlotPerSquadra);
		});
		rebuildAcquistiPerSquadra();
		rebuildAdminElencoImpersonazione();

		$rootScope.wizardFinishSetup = function() {
			if ($rootScope.calciatori && $rootScope.calciatori.length > 0) {
				$rootScope.setupInProgress = false;
				$rootScope.setupCompletato = true;
				$rootScope.showSettings = false;
				$rootScope.wizardStep = 1;
				return $q.when();
			}
			alert('Carica il file delle quotazioni prima di continuare.');
			return $q.when();
		};

		$rootScope.resetAstaIdleDev = function() {
			return $resource('./test/reset-asta', {}).save().$promise.then(function() {
				$rootScope.clearOfferta && $rootScope.clearOfferta();
			});
		};

		$rootScope.wizardCompleteSetup = function() {
			if (!$rootScope.calciatori || $rootScope.calciatori.length === 0) {
				alert('Carica il file delle quotazioni prima di andare in home.');
				return $q.when();
			}
			$rootScope.isAdminBootstrap = false;
			$rootScope.setupInProgress = false;
			$rootScope.setupCompletato = true;
			$rootScope.showSettings = false;
			if ($rootScope.markSetupTeamsSaved) {
				$rootScope.markSetupTeamsSaved(false);
			}
			if ($rootScope.markSetupComplete) {
				$rootScope.markSetupComplete(true);
			}
			$rootScope.wizardUploadCount = null;
			return $rootScope.ricalineaSessioneAdmin().then(function() {
				return $rootScope.ricaricaIndex(false);
			});
		};

		$rootScope.$watch('durataAstaDefault', function(n, o) {
			if (n > 0 && n !== o && $rootScope.wizardStep === 2 && $rootScope.setupInProgress && !$rootScope.showSettings) {
				$rootScope.syncDurataAstaFromConfig && $rootScope.syncDurataAstaFromConfig();
			}
		});

		['wizardStep', 'wizardBusy', 'showSettings', 'config', 'setupInProgress', 'wizardUploadCount',
			'budget', 'durataAstaDefault', 'maxP', 'maxD', 'maxC', 'maxA', 'isATurni', 'isSingle', 'isMantra', 'numeroUtenti'
		].forEach(function(field) {
			$rootScope.$watch(field, updateWizardFooterState);
		});
		$rootScope.$watch(function() {
			return ($rootScope.calciatori || []).length;
		}, updateWizardFooterState);
		$rootScope.$watch(function() {
			if (!$rootScope.showSettings) {
				return '';
			}
			return JSON.stringify(normalizeTeamSnapshot($rootScope.elencoAllenatori));
		}, updateWizardFooterState);
		updateWizardFooterState();

		$rootScope.$watch('faseAsta', function(n) {
			if (n === 'BIDDING' && window.innerWidth < 900) {
				$rootScope.uiTab = 'asta';
			}
		});

		$rootScope.$watch('config', function(c, prev) {
			if (c) {
				$rootScope.adminLoginNome = BOOTSTRAP_ADMIN_NOME;
				$rootScope.setupCompletato = false;
				if (!$rootScope.showSettings) {
					$rootScope.wizardStep = 1;
				}
			}
		}, true);

		$rootScope.$watch('nomegiocatore', function(n) {
			if (n) {
				syncWizardStepAfterInit();
			}
		});

		$rootScope.cancellaLega = function() {
			if (!window.confirm('Sei sicuro di voler cancellare la lega?')) {
				return $q.when();
			}
			return $rootScope.azzera && $rootScope.azzera(true);
		};

		$rootScope.$watch('numeroUtenti', function(n) {
			var count = parseInt(n, 10) || 0;
			var slots = [];
			for (var i = 0; i < count; i++) {
				slots.push(i);
			}
			$rootScope.wizardSlotPreview = slots;
		});
	}]);

})();
