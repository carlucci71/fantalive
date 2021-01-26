DROP TABLE allenatori;
DROP TABLE fantarose;
DROP TABLE giocatori;
DROP TABLE tbl;
drop table prenota;
drop table utenti;


CREATE TABLE `allenatori` (
  `Id` varchar(100) DEFAULT NULL,
  `Nome` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dump dei dati per la tabella `allenatori`
--

INSERT INTO allenatori (Id, Nome) VALUES ('1', 'Daniele');
INSERT INTO allenatori (Id, Nome) VALUES ('2', 'Concetto');
INSERT INTO allenatori (Id, Nome) VALUES ('3', 'Claudio');
INSERT INTO allenatori (Id, Nome) VALUES ('4', 'New');
INSERT INTO allenatori (Id, Nome) VALUES ('5', 'Dante');
INSERT INTO allenatori (Id, Nome) VALUES ('6', 'Giovanni');
INSERT INTO allenatori (Id, Nome) VALUES ('7', 'Oppe');
INSERT INTO allenatori (Id, Nome) VALUES ('8', 'Roby');

-- --------------------------------------------------------

--
-- Struttura della tabella `fantarose`
--

CREATE TABLE `fantarose` (
  `idGiocatore` varchar(100) NOT NULL,
  `idAllenatore` varchar(100) NOT NULL,
  `Costo` varchar(100) NOT NULL,
  `sqltime` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dump dei dati per la tabella `fantarose`
--

INSERT INTO `fantarose` (`idGiocatore`, `idAllenatore`, `Costo`, `sqltime`) VALUES
('1017726', '1', '1', '2019-09-04 17:57:02:790');

-- --------------------------------------------------------

--
-- Struttura della tabella `giocatori`
--

CREATE TABLE `giocatori` (
  `Id` varchar(100) DEFAULT NULL,
  `Squadra` varchar(100) DEFAULT NULL,
  `Nome` varchar(100) DEFAULT NULL,
  `Ruolo` varchar(100) DEFAULT NULL,
  `quotazione` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dump dei dati per la tabella `giocatori`
--

INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1051299', 'Parma', 'F. Alastra', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1026722', 'Brescia', 'E. Alfonso', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1049764', 'Brescia', 'L. Andrenacci', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1020740', 'Bologna', ' Angelo da Costa', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1046658', 'Cagliari', 'S. Aresti', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1048586', 'Sampdoria', 'E. Audero', 'P', '9');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1069940', 'Sampdoria', 'L. Avogadri', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017883', 'Hellas Verona', 'A. Berardi', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1029919', 'SPAL', 'E. Berisha', 'P', '12');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1023755', 'Inter', 'T. Berni', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1063106', 'Lecce', 'M. Bleve', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017798', 'Juventus', 'G. Buffon', 'P', '2');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021307', 'Parma', 'S. Colombi', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018116', 'Sassuolo', 'A. Consigli', 'P', '11');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1035189', 'Cagliari', 'A. Cragno', 'P', '14');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1067764', 'Roma', ' Daniel Fuzato', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1050300', 'Milan', 'G. Donnarumma', 'P', '19');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021368', 'Milan', 'A. Donnarumma', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1052894', 'Fiorentina', 'B. Dragowski', 'P', '11');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1038409', 'Sampdoria', 'W. Falcone', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1037186', 'Lecce', ' Gabriel', 'P', '9');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1061992', 'Fiorentina', 'S. Ghidotti', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1048944', 'Atalanta', 'P. Gollini', 'P', '14');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1038976', 'Lazio', 'G. Guerrieri', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017919', 'Inter', 'S. Handanovic', 'P', '21');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1070284', 'Genoa', ' Jandrei', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1044405', 'Brescia', 'J. Joronen', 'P', '11');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1019469', 'Napoli', 'O. Karnezis', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1045083', 'SPAL', 'K. Letica', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017885', 'Genoa', 'F. Marchetti', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1073018', 'SPAL', 'M. Meneghetti', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1049089', 'Napoli', 'A. Meret', 'P', '14');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017826', 'Roma', 'A. Mirante', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1067778', 'Udinese', 'J. Musso', 'P', '10');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021069', 'Udinese', ' Nicolas', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1020324', 'Cagliari', 'R. Olsen', 'P', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018588', 'Napoli', 'D. Ospina', 'P', '2');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017920', 'Inter', 'D. Padelli', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1045222', 'Roma', ' Pau Lopez', 'P', '14');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018055', 'Sassuolo', 'G. Pegolo', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1016670', 'Milan', ' Pepe Reina', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021155', 'Juventus', 'M. Perin', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1050783', 'Udinese', 'S. Perisan', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1020980', 'Juventus', 'C. Pinsoglio', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1060787', 'Milan', 'A. Plizzari', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021809', 'Lazio', 'S. Proto', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1051634', 'Genoa', 'I. Radu', 'P', '11');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1051808', 'Hellas Verona', 'B. Radunovic', 'P', '2');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021070', 'Cagliari', ' Rafael', 'P', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018237', 'Torino', 'A. Rosati', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1061828', 'Atalanta', 'F. Rossi', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1069265', 'Sassuolo', 'A. Russo', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1050360', 'Bologna', 'M. Sarr', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021308', 'Sampdoria', 'A. Seculin', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1026743', 'Parma', 'L. Sepe', 'P', '10');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1035165', 'Hellas Verona', 'M. Silvestri', 'P', '11');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018396', 'Torino', 'S. Sirigu', 'P', '17');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1038871', 'Bologna', 'L. Skorupski', 'P', '13');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1040905', 'Atalanta', 'M. Sportiello', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1038959', 'Lazio', 'T. Strakosha', 'P', '16');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1016572', 'Juventus', 'W. Szczesny', 'P', '18');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018210', 'Fiorentina', 'P. Terracciano', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1061406', 'SPAL', 'D. Thiam', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018329', 'Torino', 'S. Ujkani', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1026272', 'Lecce', 'M. Vigorito', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1059628', 'Genoa', 'R. Vodisek', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1045422', 'Torino', 'A. Zaccagno', 'P', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017958', 'Lazio', 'F. Acerbi', 'D', '16');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1058060', 'Hellas Verona', 'C. Adjapong', 'D', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1046802', 'Torino', 'O. Aina', 'D', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1044448', 'Hellas Verona', ' Alan Empereur', 'D', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1019957', 'Juventus', ' Alex Sandro', 'D', '20');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1019903', 'Genoa', 'P. Ankersen', 'D', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017936', 'Inter', 'K. Asamoah', 'D', '10');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1064419', 'Sampdoria', 'T. Augello', 'D', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1040930', 'Bologna', 'M. Bani', 'D', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1045011', 'Genoa', 'A. Barreca', 'D', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1061306', 'Inter', 'A. Bastoni', 'D', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1048799', 'Lazio', ' Bastos', 'D', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1060332', 'Lecce', 'R. Benzar', 'D', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1036233', 'Sampdoria', 'B. Bereszynski', 'D', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021310', 'Inter', 'C. Biraghi', 'D', '9');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1029844', 'Genoa', 'D. Biraschi', 'D', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1020137', 'Hellas Verona', 'S. Bocchetti', 'D', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1044224', 'Torino', 'K. Bonifazi', 'D', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017802', 'Juventus', 'L. Bonucci', 'D', '16');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1067789', 'Torino', ' Bremer', 'D', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1020079', 'Parma', ' Bruno Alves', 'D', '13');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021215', 'Cagliari', 'F. Cacciatore', 'D', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017803', 'Fiorentina', 'M. Caceres', 'D', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1050066', 'Milan', 'D. Calabria', 'D', '9');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1044123', 'Milan', 'M. Caldara', 'D', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021342', 'Lecce', 'M. Calderoni', 'D', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1040779', 'Atalanta', 'T. Castagne', 'D', '16');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1035172', 'Fiorentina', 'F. Ceccherini', 'D', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021246', 'Cagliari', 'L. Ceppitelli', 'D', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1076687', 'Roma', 'M. Cetin', 'D', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1071604', 'Sampdoria', 'J. Chabot', 'D', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1073066', 'Brescia', 'J. Chancellor', 'D', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017804', 'Juventus', 'G. Chiellini', 'D', '20');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1023840', 'Sassuolo', 'V. Chiriches', 'D', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1023674', 'SPAL', 'T. Cionek', 'D', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1049765', 'Brescia', 'A. Cistana', 'D', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1039512', 'Sampdoria', 'O. Colley', 'D', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1046678', 'Milan', 'A. Conti', 'D', '9');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1064558', 'Bologna', 'G. Corbo', 'D', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021247', 'Hellas Verona', 'A. Crescenzi', 'D', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1020075', 'Genoa', 'D. Criscito', 'D', '14');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1052930', 'Fiorentina', ' Dalbert Henrique', 'D', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1020948', 'Inter', 'D. D Ambrosio', 'D', '9');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1019956', 'Juventus', ' Danilo', 'D', '13');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017931', 'Bologna', ' Danilo', 'D', '9');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1020949', 'Parma', 'M. Darmian', 'D', '10');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1046580', 'Hellas Verona', 'P. Dawidowicz', 'D', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1060600', 'Juventus', 'M. de Ligt', 'D', '22');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1020776', 'Udinese', 'S. De Maio', 'D', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017734', 'Juventus', 'M. De Sciglio', 'D', '10');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018182', 'Torino', 'L. De Silvestri', 'D', '12');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1029803', 'Inter', 'S. de Vrij', 'D', '19');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1040803', 'Lecce', 'C. Dell Orco', 'D', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1070095', 'Juventus', 'M. Demiral', 'D', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1019813', 'Bologna', 'S. Denswil', 'D', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1058026', 'Sampdoria', 'F. Depaoli', 'D', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1043092', 'Parma', 'K. Dermaku', 'D', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1020809', 'Napoli', 'G. Di Lorenzo', 'D', '17');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1037256', 'Bologna', 'M. Dijks', 'D', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1049778', 'Inter', 'F. Dimarco', 'D', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1042656', 'Torino', 'K. Djidji', 'D', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1020379', 'Atalanta', 'B. Djimsiti', 'D', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1068474', 'Lecce', 'L. Dumancic', 'D', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1022248', 'Lazio', 'R. Durmisi', 'D', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1065826', 'Genoa', 'J. El Yamiq', 'D', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017859', 'Hellas Verona', 'D. Faraoni', 'D', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1045439', 'SPAL', 'M. Fares', 'D', '10');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1016115', 'Roma', 'F. Fazio', 'D', '16');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018183', 'SPAL', ' Felipe', 'D', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1065753', 'Brescia', ' Felipe Curcio', 'D', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1046656', 'Sassuolo', 'G. Ferrari', 'D', '13');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1040419', 'Sampdoria', 'A. Ferrari', 'D', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021227', 'Lecce', 'R. Fiamozzi', 'D', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021112', 'Roma', 'A. Florenzi', 'D', '17');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1062553', 'Milan', 'M. Gabbia', 'D', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1043036', 'Parma', 'R. Gagliolo', 'D', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1069407', 'Lecce', 'A. Gallo', 'D', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1020746', 'Brescia', 'D. Gastaldello', 'D', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1050006', 'Genoa', 'P. Ghiglione', 'D', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018714', 'Napoli', 'F. Ghoulam', 'D', '12');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1016081', 'Inter', 'D. Godin', 'D', '20');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1039172', 'Genoa', 'E. Goldaniga', 'D', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1059906', 'Atalanta', 'R. Gosens', 'D', '15');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1065620', 'Atalanta', ' Guilherme Arana', 'D', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1038516', 'Hellas Verona', 'K. Gunter', 'D', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1047300', 'Atalanta', 'H. Hateboer', 'D', '17');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1058087', 'Milan', 'T. Hernandez', 'D', '11');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1020915', 'Napoli', 'E. Hysaj', 'D', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1027258', 'Parma', 'S. Iacoponi', 'D', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1070483', 'Atalanta', ' Ibanez', 'D', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1062418', 'SPAL', ' Igor', 'D', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1026747', 'Torino', 'A. Izzo', 'D', '21');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017861', 'Roma', ' Juan Jesus', 'D', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1043465', 'Sassuolo', 'G. Kiriakopoulos', 'D', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017773', 'Atalanta', 'S. Kjaer', 'D', '10');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1023390', 'Cagliari', 'R. Klavan', 'D', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1016928', 'Roma', 'A. Kolarov', 'D', '27');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1035475', 'Napoli', 'K. Koulibaly', 'D', '19');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1064677', 'Hellas Verona', 'M. Kumbulla', 'D', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1022263', 'Udinese', 'J. Larsen', 'D', '10');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1035185', 'Parma', 'V. Laurini', 'D', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1040811', 'Torino', 'D. Laxalt', 'D', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1076539', 'Milan', ' Leo Duarte', 'D', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1037688', 'Lecce', 'F. Lucioni', 'D', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1060977', 'Lazio', ' Luiz Felipe', 'D', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021814', 'Lazio', 'J. Lukaku', 'D', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1043566', 'Napoli', 'S. Luperto', 'D', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1063043', 'Torino', ' Lyanco', 'D', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1019506', 'Cagliari', 'C. Lykogiannis', 'D', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1065822', 'Brescia', 'G. Magnani', 'D', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1024122', 'Napoli', 'N. Maksimovic', 'D', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1053456', 'Napoli', 'K. Malcuit', 'D', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1049438', 'Roma', 'G. Mancini', 'D', '15');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1054008', 'Brescia', 'M. Mangraviti', 'D', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1022704', 'Napoli', 'K. Manolas', 'D', '19');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021390', 'Napoli', ' Mario Rui', 'D', '9');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1060559', 'Sassuolo', ' Marlon', 'D', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1040943', 'Brescia', 'B. Martella', 'D', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1063096', 'Lazio', 'A. Marusic', 'D', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018125', 'Atalanta', 'A. Masiello', 'D', '9');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1051344', 'Brescia', 'A. Mateju', 'D', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1045106', 'Cagliari', 'F. Mattiello', 'D', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1035924', 'Bologna', 'I. M baye', 'D', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1038130', 'Lecce', 'B. Meccariello', 'D', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1058528', 'Fiorentina', 'N. Milenkovic', 'D', '14');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1068522', 'Sassuolo', 'M. Muldur', 'D', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1037787', 'Sampdoria', 'J. Murillo', 'D', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018155', 'Sampdoria', 'N. Murru', 'D', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1016180', 'Milan', 'M. Musacchio', 'D', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018507', 'Torino', 'N. N Koulou', 'D', '15');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1038550', 'Udinese', 'B. Nuytinck', 'D', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1067779', 'Udinese', 'N. Opoku', 'D', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1022057', 'Genoa', 'M. Pajac', 'D', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1048189', 'Atalanta', 'J. Palomino', 'D', '11');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1044231', 'Lazio', ' Patric', 'D', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1067783', 'Bologna', 'N. Paz', 'D', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1061624', 'Cagliari', 'L. Pellegrini', 'D', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018127', 'Sassuolo', 'F. Peluso', 'D', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1052316', 'Parma', 'G. Pezzella', 'D', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1051714', 'Fiorentina', 'G. Pezzella', 'D', '13');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1053975', 'Cagliari', 'S. Pinna', 'D', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1037836', 'Cagliari', 'F. Pisacane', 'D', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1060452', 'Fiorentina', ' Pol Lirola', 'D', '11');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017895', 'Lazio', 'S. Radu', 'D', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1044683', 'Atalanta', ' Rafael Toloi', 'D', '9');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1064713', 'Fiorentina', 'L. Ranieri', 'D', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017862', 'Inter', 'A. Ranocchia', 'D', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1061728', 'Fiorentina', 'J. Rasmussen', 'D', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1067785', 'SPAL', 'A. Reca', 'D', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1020918', 'Sampdoria', 'V. Regini', 'D', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1045519', 'Lecce', 'D. Riccardi', 'D', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1020749', 'Lecce', 'A. Rispoli', 'D', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1067900', 'Udinese', ' Rodrigo Becao', 'D', '10');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017403', 'Milan', 'R. Rodriguez', 'D', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1058044', 'Sassuolo', ' Rogerio', 'D', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1045105', 'Sassuolo', 'F. Romagna', 'D', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1038408', 'Milan', 'A. Romagnoli', 'D', '17');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1067793', 'Genoa', 'C. Romero', 'D', '12');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018060', 'Lecce', 'L. Rossettini', 'D', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1051495', 'Hellas Verona', 'A. Rrahmani', 'D', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1039349', 'Juventus', 'D. Rugani', 'D', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1035198', 'Brescia', 'S. Sabelli', 'D', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017385', 'SPAL', 'J. Sala', 'D', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1058027', 'Udinese', ' Samir', 'D', '9');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1016704', 'Roma', 'D. Santon', 'D', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1052503', 'Brescia', 'A. Semprini', 'D', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1064686', 'Udinese', 'F. Sierralta', 'D', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1070903', 'Torino', 'W. Singo', 'D', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1024244', 'Inter', 'M. Skriniar', 'D', '17');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1016642', 'Roma', 'C. Smalling', 'D', '13');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1026211', 'Roma', 'L. Spinazzola', 'D', '10');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1049814', 'Udinese', 'H. ter Avest', 'D', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1063462', 'Fiorentina', 'A. Terzic', 'D', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1040322', 'Sassuolo', 'J. Toljan', 'D', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1069764', 'Bologna', 'T. Tomiyasu', 'D', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018032', 'SPAL', 'N. Tomovic', 'D', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1020920', 'Napoli', 'L. Tonelli', 'D', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1069405', 'Sassuolo', 'A. Tripaldelli', 'D', '2');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1050996', 'Udinese', 'W. Troost-Ekong', 'D', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1040304', 'Lazio', 'D. Vavro', 'D', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1044578', 'Fiorentina', 'L. Venuti', 'D', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1071770', 'Lecce', 'B. Vera', 'D', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1040446', 'SPAL', 'F. Vicari', 'D', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018003', 'Hellas Verona', 'L. Vitale', 'D', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1070336', 'Cagliari', 'S. Walukiewicz', 'D', '2');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1077115', 'Hellas Verona', ' Wesley', 'D', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1016185', 'Genoa', 'C. Zapata', 'D', '9');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1043014', 'Roma', 'D. Zappacosta', 'D', '11');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1072032', 'Inter', 'L. Agoume', 'C', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1076585', 'Genoa', 'K. Agudelo', 'C', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017935', 'Hellas Verona', 'E. Agyemang-Badu', 'C', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1059486', 'Torino', ' Alex Berenguer', 'C', '15');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1037192', 'Napoli', ' Allan', 'C', '16');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1063071', 'Hellas Verona', 'S. Amrabat', 'C', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1069588', 'Lazio', ' Andre Anderson', 'C', '2');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1020136', 'Torino', 'C. Ansaldi', 'C', '14');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1019244', 'Fiorentina', 'M. Badelj', 'C', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1059788', 'Udinese', 'A. Barak', 'C', '10');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1045456', 'Inter', 'N. Barella', 'C', '18');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1020814', 'Parma', 'A. Barilla', 'C', '11');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018099', 'Sampdoria', 'E. Barreto', 'C', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021194', 'Torino', 'D. Baselli', 'C', '17');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1065356', 'Fiorentina', 'N. Beloko', 'C', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1038045', 'Fiorentina', 'M. Benassi', 'C', '17');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1053758', 'Milan', 'I. Bennacer', 'C', '12');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1063039', 'Juventus', 'R. Bentancur', 'C', '13');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1029676', 'Lazio', 'V. Berisha', 'C', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1039156', 'Juventus', 'F. Bernardeschi', 'C', '17');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021819', 'Milan', 'L. Biglia', 'C', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018285', 'Cagliari', 'V. Birsa', 'C', '11');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1060395', 'Brescia', 'D. Bisoli', 'C', '11');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018130', 'Milan', 'G. Bonaventura', 'C', '14');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017791', 'Milan', 'F. Borini', 'C', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1016197', 'Inter', ' Borja Valero', 'C', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1052588', 'Sassuolo', 'M. Bourabia', 'C', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1038700', 'Inter', 'M. Brozovic', 'C', '19');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1020934', 'Parma', 'G. Brugman', 'C', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1042724', 'Milan', 'H. Calhanoglu', 'C', '16');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017227', 'Juventus', 'E. Can', 'C', '16');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017903', 'Inter', 'A. Candreva', 'C', '10');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1058677', 'Genoa', 'F. Cassata', 'C', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1037698', 'Cagliari', 'L. Castro', 'C', '14');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1049214', 'Fiorentina', 'G. Castrovilli', 'C', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1040044', 'Lazio', 'D. Cataldi', 'C', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1060742', 'Fiorentina', 'F. Chiesa', 'C', '25');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018135', 'Cagliari', 'L. Cigarini', 'C', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017744', 'Roma', 'B. Cristante', 'C', '16');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1067787', 'Fiorentina', ' Cristobal Montiel', 'C', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1043406', 'Fiorentina', 'S. Cristoforo', 'C', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018035', 'Juventus', 'J. Cuadrado', 'C', '12');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018783', 'Fiorentina', 'B. Dabo', 'C', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021092', 'SPAL', 'M. D Alessandro', 'C', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1039237', 'Hellas Verona', ' Daniel Bessa', 'C', '10');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1064716', 'Hellas Verona', 'A. Danzi', 'C', '2');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1046601', 'Udinese', 'R. de Paul', 'C', '21');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1036130', 'Atalanta', 'M. de Roon', 'C', '16');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1040467', 'Cagliari', 'A. Deiola', 'C', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018162', 'Brescia', 'D. Dessena', 'C', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1051219', 'Roma', 'A. Diawara', 'C', '9');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1029769', 'Sassuolo', 'F. Djuricic', 'C', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1020511', 'Juventus', ' Douglas Costa', 'C', '21');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1029295', 'Sassuolo', 'A. Duncan', 'C', '16');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018251', 'Bologna', 'B. Dzemaili', 'C', '9');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018163', 'Sampdoria', 'A. Ekdal', 'C', '10');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1053662', 'Napoli', 'E. Elmas', 'C', '10');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1062376', 'SPAL', ' Espeto', 'C', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1038483', 'Fiorentina', 'V. Eysseric', 'C', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1051717', 'Napoli', ' Fabian Ruiz', 'C', '23');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1037875', 'Cagliari', 'P. Farago', 'C', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1050895', 'Udinese', 'S. Fofana', 'C', '12');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1047609', 'Atalanta', 'R. Freuler', 'C', '17');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1067792', 'Napoli', 'G. Gaetano', 'C', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1038964', 'Inter', 'R. Gagliardini', 'C', '10');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1038058', 'Fiorentina', 'R. Ghezzal', 'C', '13');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018222', 'Atalanta', 'A. Gomez', 'C', '30');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1049181', 'Parma', 'A. Grassi', 'C', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1043574', 'Hellas Verona', 'L. Henderson', 'C', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1061643', 'Parma', ' Hernani', 'C', '10');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1042704', 'Lecce', 'G. Imbula', 'C', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1029647', 'Cagliari', 'A. Ionita', 'C', '9');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1059955', 'Genoa', 'F. Jagiello', 'C', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017650', 'Udinese', 'M. Jajalo', 'C', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1049690', 'Sampdoria', 'J. Jankto', 'C', '10');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1051738', 'Lazio', ' Jony', 'C', '12');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1015994', 'Napoli', ' Jose Callejon', 'C', '27');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1050707', 'Milan', 'F. Kessie', 'C', '20');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1015999', 'Juventus', 'S. Khedira', 'C', '13');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1061550', 'Roma', 'J. Kluivert', 'C', '16');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1022174', 'Bologna', 'L. Krejci', 'C', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1051217', 'Milan', 'R. Krunic', 'C', '13');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018279', 'Parma', 'J. Kucka', 'C', '15');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1066419', 'Parma', 'D. Kulusevski', 'C', '2');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021228', 'SPAL', 'J. Kurtic', 'C', '16');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1029918', 'Inter', 'V. Lazaro', 'C', '14');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1024132', 'Hellas Verona', 'D. Lazovic', 'C', '10');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1059671', 'Lazio', 'M. Lazzari', 'C', '14');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1062750', 'Genoa', 'L. Lerager', 'C', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1062611', 'Sampdoria', 'M. Leris', 'C', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1038480', 'Sampdoria', 'K. Linetty', 'C', '14');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1050523', 'Sassuolo', 'M. Locatelli', 'C', '12');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1069410', 'Hellas Verona', ' Lucas Felippe', 'C', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1016686', 'Lazio', ' Lucas Leiva', 'C', '13');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1069597', 'Milan', ' Lucas Paqueta', 'C', '19');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1016137', 'Lazio', ' Luis Alberto', 'C', '27');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1048457', 'Torino', 'S. Lukic', 'C', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017908', 'Lazio', 'S. Lulic', 'C', '16');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021418', 'Sassuolo', 'F. Magnanelli', 'C', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1041852', 'Lecce', 'Ž. Majer', 'C', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1048559', 'Atalanta', 'R. Malinovskiy', 'C', '12');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1068478', 'Lecce', 'M. Mancosu', 'C', '13');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1049183', 'Udinese', 'R. Mandragora', 'C', '11');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1071759', 'Sampdoria', 'G. Maroni', 'C', '9');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018412', 'Juventus', 'B. Matuidi', 'C', '15');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1061634', 'SPAL', 'S. Mawuli', 'C', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1044328', 'Sassuolo', 'L. Mazzitelli', 'C', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1016126', 'Bologna', 'G. Medel', 'C', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018484', 'Torino', 'S. Meite', 'C', '13');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018282', 'Hellas Verona', ' Miguel Veloso', 'C', '9');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1044219', 'Lazio', 'S. Milinkovic-Savic', 'C', '26');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021419', 'SPAL', 'S. Missiroli', 'C', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1020517', 'Roma', 'H. Mkhitaryan', 'C', '24');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1020756', 'Parma', 'G. Munari', 'C', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1049403', 'SPAL', 'A. Murgia', 'C', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018166', 'Cagliari', 'R. Nainggolan', 'C', '17');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1052374', 'Cagliari', 'N. Nandez', 'C', '14');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1058045', 'Brescia', 'E. Ndoj', 'C', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1070417', 'Cagliari', 'C. Oliva', 'C', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1068631', 'Bologna', 'A. Olsen', 'C', '11');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018259', 'Genoa', 'G. Pandev', 'C', '12');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018313', 'Lazio', 'M. Parolo', 'C', '13');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1040264', 'Atalanta', 'M. Pasalic', 'C', '18');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018415', 'Roma', 'J. Pastore', 'C', '13');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1020757', 'Sassuolo', ' Pedro Obiang', 'C', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1045397', 'Roma', 'L. Pellegrini', 'C', '18');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1016128', 'Roma', 'D. Perotti', 'C', '17');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1051202', 'Hellas Verona', 'M. Pessina', 'C', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1060382', 'Lecce', 'J. Petriccione', 'C', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017784', 'Juventus', 'M. Pjanic', 'C', '21');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017872', 'Bologna', 'A. Poli', 'C', '11');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1052105', 'Fiorentina', 'E. Pulgar', 'C', '22');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1038053', 'Juventus', 'A. Rabiot', 'C', '18');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018347', 'Genoa', 'I. Radovanovic', 'C', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018011', 'Sampdoria', 'G. Ramirez', 'C', '15');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1016591', 'Juventus', 'A. Ramsey', 'C', '21');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017232', 'Fiorentina', 'F. Ribery', 'C', '22');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017384', 'Torino', 'T. Rincon', 'C', '13');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1047712', 'Cagliari', 'M. Rog', 'C', '9');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018190', 'Brescia', ' Rômulo', 'C', '10');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1048239', 'Milan', ' Samu Castillejo', 'C', '12');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1020939', 'Genoa', 'R. Saponara', 'C', '9');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1029427', 'Genoa', 'L. Schone', 'C', '15');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1071781', 'Bologna', 'J. Schouten', 'C', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021325', 'Parma', 'M. Scozzarella', 'C', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1061677', 'Udinese', 'K. Sema', 'C', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1039153', 'Inter', 'S. Sensi', 'C', '15');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1024724', 'Lecce', 'E. Shakhov', 'C', '10');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1020760', 'Bologna', 'R. Soriano', 'C', '16');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1047517', 'Brescia', 'N. Spalek', 'C', '9');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018284', 'Genoa', 'S. Sturaro', 'C', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1038729', 'Milan', ' Suso', 'C', '22');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1050976', 'Bologna', 'M. Svanberg', 'C', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1037852', 'Lecce', 'A. Tabanelli', 'C', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021089', 'Lecce', 'P. Tachtsidis', 'C', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1040227', 'Sampdoria', 'M. Thorsby', 'C', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1064571', 'Brescia', 'S. Tonali', 'C', '11');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1064555', 'Sassuolo', 'H. Traore', 'C', '16');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1026729', 'Brescia', 'L. Tremolada', 'C', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1059342', 'Roma', 'C. Ünder', 'C', '24');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1020933', 'SPAL', 'M. Valdifiori', 'C', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017754', 'SPAL', 'M. Valoti', 'C', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1039529', 'Inter', 'M. Vecino', 'C', '14');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1020975', 'Torino', 'S. Verdi', 'C', '12');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1042674', 'Roma', 'J. Veretout', 'C', '17');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017788', 'Hellas Verona', 'V. Verre', 'C', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1069297', 'Sampdoria', 'R. Vieira Nan', 'C', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1066299', 'Brescia', 'M. Viviani', 'C', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1058838', 'Udinese', ' Walace', 'C', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017479', 'Napoli', 'A. Younes', 'C', '13');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1043177', 'Hellas Verona', 'M. Zaccagni', 'C', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1061639', 'Roma', 'N. Zaniolo', 'C', '21');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1038482', 'Napoli', 'P. Zielinski', 'C', '22');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1059790', 'Brescia', 'J. Zmrhal', 'C', '10');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1067311', 'Fiorentina', 'S. Zurkowski', 'C', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1071742', 'Lazio', 'B. Adekanye', 'A', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1070821', 'Parma', 'A. Adorante', 'A', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1063382', 'Roma', 'M. Antonucci', 'A', '2');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1071845', 'Brescia', 'F. Aye', 'A', '13');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1016345', 'Lecce', 'K. Babacar', 'A', '11');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1016946', 'Brescia', 'M. Balotelli', 'A', '23');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1065584', 'Atalanta', 'M. Barrow', 'A', '10');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021062', 'Torino', 'A. Belotti', 'A', '34');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1035059', 'Sassuolo', 'D. Berardi', 'A', '24');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017741', 'Fiorentina', 'K. Boateng', 'A', '20');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1046806', 'Sassuolo', 'J. Boga', 'A', '13');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1044263', 'Sampdoria', 'F. Bonazzoli', 'A', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1023949', 'Lazio', 'F. Caicedo', 'A', '16');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021140', 'Sampdoria', 'G. Caprari', 'A', '13');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021265', 'Sassuolo', 'F. Caputo', 'A', '26');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1040091', 'Cagliari', 'A. Cerri', 'A', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1019340', 'Parma', 'A. Cornelius', 'A', '10');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1049827', 'Lazio', 'J. Correa', 'A', '23');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1016003', 'Juventus', ' Cristiano Ronaldo', 'A', '52');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1037855', 'Sassuolo', 'G. Defrel', 'A', '17');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018079', 'Bologna', 'M. Destro', 'A', '12');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021207', 'Hellas Verona', 'S. Di Carmine', 'A', '13');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1040186', 'SPAL', 'F. Di Francesco', 'A', '12');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1043047', 'Hellas Verona', 'A. Di Gaudio', 'A', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021302', 'Lecce', ' Diego Farias', 'A', '12');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1026287', 'Brescia', 'A. Donnarumma', 'A', '21');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1050043', 'Lecce', 'E. Dubickas', 'A', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1037689', 'Juventus', 'P. Dybala', 'A', '30');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1016947', 'Roma', 'E. Dzeko', 'A', '29');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1049609', 'Torino', 'S. Edera', 'A', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1070991', 'Inter', 'S. Esposito', 'A', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1037822', 'Lecce', 'F. Falco', 'A', '10');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1049829', 'Genoa', 'A. Favilli', 'A', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017846', 'SPAL', 'S. Floccari', 'A', '11');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018143', 'Sampdoria', 'M. Gabbiadini', 'A', '14');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1016596', 'Parma', 'Y. Gervinho', 'A', '31');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1049048', 'Genoa', 'S. Gumus', 'A', '11');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1016004', 'Juventus', 'G. Higuain', 'A', '29');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1026011', 'Torino', ' Iago Falque', 'A', '23');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018103', 'Atalanta', 'J. Ilicic', 'A', '32');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021148', 'Lazio', 'C. Immobile', 'A', '41');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1043533', 'Parma', 'R. Inglese', 'A', '22');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021149', 'Napoli', 'L. Insigne', 'A', '35');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1053537', 'SPAL', 'M. Jankovic', 'A', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1041684', 'Cagliari', ' Joao Pedro', 'A', '19');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1024726', 'Roma', 'N. Kalinic', 'A', '16');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1060715', 'Parma', 'Y. Karamoh', 'A', '12');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1060534', 'Genoa', 'C. Kouame', 'A', '19');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1059153', 'Lecce', 'A. La Mantia', 'A', '14');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1037856', 'Lecce', 'G. Lapadula', 'A', '12');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1046705', 'Udinese', 'K. Lasagna', 'A', '16');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1067777', 'Inter', ' Lautaro Martinez', 'A', '30');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1016072', 'Napoli', ' Llorente', 'A', '15');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1060747', 'Lecce', 'S. Lo Faso', 'A', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1058072', 'Napoli', 'H. Lozano', 'A', '29');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1016625', 'Inter', 'R. Lukaku', 'A', '37');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017424', 'Juventus', 'M. Mandzukic', 'A', '18');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017821', 'Brescia', 'A. Matri', 'A', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1023438', 'Napoli', 'D. Mertens', 'A', '34');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1038874', 'Napoli', 'A. Milik', 'A', '31');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1065603', 'Torino', 'V. Millico', 'A', '2');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1044159', 'SPAL', 'G. Moncini', 'A', '13');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1045285', 'Brescia', 'L. Morosini', 'A', '7');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018048', 'Atalanta', 'L. Muriel', 'A', '22');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1025367', 'Udinese', 'I. Nestorovski', 'A', '18');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017848', 'Udinese', 'S. Okaka', 'A', '17');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1052468', 'Bologna', 'R. Orsolini', 'A', '23');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1018289', 'Bologna', 'R. Palacio', 'A', '16');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017983', 'SPAL', 'A. Paloschi', 'A', '9');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1040997', 'Torino', 'V. Parigini', 'A', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1027367', 'Cagliari', 'L. Pavoletti', 'A', '26');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017880', 'Hellas Verona', 'G. Pazzini', 'A', '12');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1069598', 'Fiorentina', ' Pedro', 'A', '21');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1039180', 'SPAL', 'A. Petagna', 'A', '25');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1059965', 'Milan', 'K. Piatek', 'A', '35');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1059346', 'Genoa', 'A. Pinamonti', 'A', '18');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1042307', 'Juventus', 'M. Pjaca', 'A', '8');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1040864', 'Inter', 'M. Politano', 'A', '19');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1067780', 'Udinese', 'I. Pussetto', 'A', '12');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017822', 'Sampdoria', 'F. Quagliarella', 'A', '37');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1065436', 'Milan', ' Rafael Leao', 'A', '21');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021399', 'Cagliari', 'D. Ragatzu', 'A', '3');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1066162', 'Sassuolo', 'G. Raspadori', 'A', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1024864', 'Milan', 'A. Rebic', 'A', '20');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1064780', 'Sampdoria', 'E. Rigoni', 'A', '18');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1064676', 'Hellas Verona', 'E. Salcedo', 'A', '2');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1044655', 'Genoa', 'A. Sanabria', 'A', '13');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1016042', 'Inter', 'A. Sanchez', 'A', '26');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1021124', 'Bologna', 'N. Sansone', 'A', '16');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1051995', 'Bologna', 'F. Santander', 'A', '15');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1020856', 'Parma', 'L. Siligardi', 'A', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1060802', 'Cagliari', 'G. Simeone', 'A', '18');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1064714', 'Fiorentina', 'R. Sottil', 'A', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1026225', 'Parma', 'M. Sprocati', 'A', '6');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1039471', 'Hellas Verona', 'M. Stepinski', 'A', '14');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1039029', 'Udinese', 'L. Teodorczyk', 'A', '10');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1017985', 'Fiorentina', 'C. Thereau', 'A', '9');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1040878', 'Brescia', 'E. Torregrossa', 'A', '12');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1061058', 'Hellas Verona', 'A. Traore', 'A', '1');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1053960', 'Hellas Verona', 'L. Tupta', 'A', '2');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1042848', 'Hellas Verona', 'G. Tutino', 'A', '5');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1058192', 'Fiorentina', 'D. Vlahovic', 'A', '4');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1043622', 'Atalanta', 'D. Zapata', 'A', '38');
INSERT INTO giocatori (Id, Squadra, Nome, Ruolo, quotazione) VALUES ('1027351', 'Torino', 'S. Zaza', 'A', '16');



-- --------------------------------------------------------

--
-- Struttura della tabella `prenota`
--

CREATE TABLE `prenota` (
  `idGiocatore` varchar(100) DEFAULT NULL,
  `Costo` varchar(100) DEFAULT NULL,
  `sqltime` varchar(100) DEFAULT NULL,
  `idAllenatore` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Struttura della tabella `tbl`
--

CREATE TABLE `tbl` (
  `Campo` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dump dei dati per la tabella `tbl`
--

INSERT INTO `tbl` (`Campo`) VALUES
('1'),
('11');

-- --------------------------------------------------------

--
-- Struttura della tabella `utenti`
--

CREATE TABLE `utenti` (
  `uname` varchar(100) DEFAULT NULL,
  `pass` varchar(100) DEFAULT NULL,
  `role` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dump dei dati per la tabella `utenti`
--

INSERT INTO `utenti` (`uname`, `pass`, `role`) VALUES
('daniele', 'daniele', 'admin'),
('ospite', 'ospite', 'ospite');

--
-- Indici per le tabelle scaricate
--

--
-- Indici per le tabelle `fantarose`
--
ALTER TABLE `fantarose`
  ADD UNIQUE KEY `MyUniqueIndexName` (`idGiocatore`,`idAllenatore`);
COMMIT;

