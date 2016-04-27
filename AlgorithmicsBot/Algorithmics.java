package AlgorithmicsBot;
// put your code here


import java.util.ArrayList;
import java.util.Collections;


public class Algorithmics implements Bot {
	// The public API of YourTeamName must not change
	// You cannot change any other classes
	// YourTeamName may not alter the state of the board or the player objects
	// It may only inspect the state of the board and the player objects
	// So you can use player.getNumUnits() but you can't use
	// player.addUnits(10000), for example

	private BoardAPI board;
	private PlayerAPI player;

	Algorithmics(BoardAPI inBoard, PlayerAPI inPlayer) {
		board = inBoard;
		player = inPlayer;
		return;
	}

	public String getName() {
		String command = "";
		command = "Algorthmics";
		return (command);
	}

	public String getReinforcement() {
		String command = "";
		ArrayList<Integer> owned_territories = new ArrayList<Integer>();
		for (int i = 0; i < GameData.NUM_COUNTRIES; i++) {
			if (board.getOccupier(i) == player.getId()) {
				owned_territories.add(i);
			}
		}

		for (int i = 0; i < owned_territories.size(); i++) {
			int j = 0;
			try {
				while (true) {
					if (board.getOccupier(GameData.ADJACENT[owned_territories.get(i)][j]) != player.getId()) {
						String target = GameData.COUNTRY_NAMES[owned_territories.get(i)];
						target = target.replaceAll("\\s", "");
						command = target + " " + 1;
					}
					j++;
				}
			} catch (Exception e) {
				if (command != "") {
					break;
				} else {
					continue;
				}
			}
		}

		return command;

	}

	public String getPlacement(int forPlayer) {
		String command = "";
		//int myId = 0;

		for (int i = 0; i < 42; i++) {
			if (forPlayer > 1) {
				if ((board.getOccupier(i) == forPlayer) && (i < 42)) {

					if (GameData.CONTINENT_IDS[i] < 6 && board.getNumUnits(i) < 5) {
						command = GameData.COUNTRY_NAMES[(int) (Math.random() * GameData.NUM_COUNTRIES)];
						command = command.replaceAll("\\s", "");
						command += " 1";

					} else if (GameData.CONTINENT_IDS[i] < 6 && board.getNumUnits(i) > 5) {
						command = GameData.COUNTRY_NAMES[(int) (Math.random() * GameData.NUM_COUNTRIES)];
						command = command.replaceAll("\\s", "");
						command += " 0";

					}

				}
			}

		}
		return (command);
	}

	public String getCardExchange() {
		boolean flagI = checkI();
		boolean flagA = checkA();
		boolean flagC = checkC();
		boolean flagF = checkFullset();
		boolean flagIW = checkIW();
		boolean flagIWW = checkIWW();
		boolean flagAW = checkAW();
		boolean flagAWW = checkAWW();
		boolean flagCW = checkCW();
		boolean flagCWW = checkCWW();
		boolean flagCAW = checkCAW();
		boolean flagIAW = checkIAW();
		boolean flagICW = checkICW();
		String command = "skip";

		if (player.isForcedExchange() == true) {

			if (flagI) {
				command = "III";
			}
			if (flagA) {
				command = "AAA";
			}
			if (flagC) {
				command = "CCC";
			}
			if (flagF) {
				command = "IAC";
			}
			if (flagIW) {
				command = "IIW";
			}
			if (flagIWW) {
				command = "IWW";
			}
			if (flagAW) {
				command = "AAW";
			}
			if (flagAWW) {
				command = "AWW";
			}
			if (flagCW) {
				command = "CCW";
			}
			if (flagCWW) {
				command = "CWW";
			}
			if (flagCAW) {
				command = "CAW";
			}
			if (flagIAW) {
				command = "IAW";
			}
			if (flagICW) {
				command = "ICW";
			}

		} else if (player.isForcedExchange() == false) {
			if (player.getCards().size() > 3) {
				if (player.isOptionalExchange()) {

					if (flagI) {
						command = "III";
					}
					if (flagA) {
						command = "AAA";
					}
					if (flagC) {
						command = "CCC";
					}
					if (flagF) {
						command = "IAC";
					}
					if (flagIW) {
						command = "IIW";
					}
					if (flagIWW) {
						command = "IWW";
					}
					if (flagAW) {
						command = "AAW";
					}
					if (flagAWW) {
						command = "AWW";
					}
					if (flagCW) {
						command = "CCW";
					}
					if (flagCWW) {
						command = "CWW";
					}
					if (flagCAW) {
						command = "CAW";
					}
					if (flagIAW) {
						command = "IAW";
					}
					if (flagICW) {
						command = "ICW";
					}
				}
			} else if (player.getCards().size() < 4) {
				command = "skip";
			}
		}

		return (command);
	}

	public String getBattle() {
		String command = "";
		Boolean success = false;
		ArrayList<Integer> owned_territories = new ArrayList<Integer>();
		for (int i = 0; i < GameData.NUM_COUNTRIES; i++) {
			if (board.getOccupier(i) == player.getId()) {
				owned_territories.add(i);
			}
		}
		for (int i = 0; i < owned_territories.size(); i++) {
			int j = 0;
			try {
				while (true) {
					if (board.getOccupier(GameData.ADJACENT[owned_territories.get(i)][j]) != player.getId()) {
						if ((double) (board.getNumUnits(GameData.ADJACENT[owned_territories.get(i)][j]))
								/ ((double) board.getNumUnits(owned_territories.get(i))) < 0.6) {
							int armies;
							String source = GameData.COUNTRY_NAMES[owned_territories.get(i)];
							String target = GameData.COUNTRY_NAMES[GameData.ADJACENT[owned_territories.get(i)][j]];

							source = source.replaceAll("\\s", "");
							target = target.replaceAll("\\s", "");

							if (board.getNumUnits(owned_territories.get(i)) > 4) {
								armies = 3;
							} else {
								armies = board.getNumUnits(owned_territories.get(i)) - 1;
							}

							command = source + " " + target + " " + armies;
						}
					}
					j++;
				}
			} catch (Exception e) {
				if (command != "") {
					success = true;
					break;
				} else {
					continue;
				}
			}
		}

		if (!success) {
			command = "skip";
		}
		return (command);
	}

	public String getDefence(int countryId) {
		String command = "";
		if (board.getNumUnits(countryId) > 1) {
			command = "2";
		} else {
			command = "1";
		}
		return (command);
	}

	public String getMoveIn(int attackCountryId) {
		String command = "";
		command = Integer.toString(board.getNumUnits(attackCountryId) - 1);
		return (command);
	}

	public String getFortify() {
		String command = "";
		/*
		 * int fortifyToCountry=-1; int fortifyFromCountry=-1; int
		 * largestAdjArmy=0; int smallestAdjArmy = 0; int numberToMove=0;
		 */
		command = "skip";
		// return command;

		ArrayList<Integer> owned_territories = new ArrayList<Integer>();
		ArrayList<Integer> owned_interior_territories = new ArrayList<Integer>();
		ArrayList<Integer> owned_border_territories = new ArrayList<Integer>();

		for (int i = 0; i < GameData.NUM_COUNTRIES; i++) {
			if (board.getOccupier(i) == player.getId()) {
				owned_territories.add(i);
			}
		}

		for (int i = 0; i < owned_territories.size(); i++) {
			int j = 0;
			try {
				while (true) {
					if (board.getOccupier(GameData.ADJACENT[owned_territories.get(i)][j]) != player.getId()) {
						owned_border_territories.add(owned_territories.get(i));
						break;
					}
					j++;
				}
			} catch (Exception e) {
				if (board.getNumUnits(owned_territories.get(i)) > 1) {
					owned_interior_territories.add(owned_territories.get(i));
				}
				continue;
			}
		}

		if (owned_interior_territories.size() == 0) {
			command = "skip";
		} else {
			for (int i = 0; i < owned_border_territories.size(); i++) {
				if (board.isConnected(owned_interior_territories.get(0), owned_border_territories.get(i))) {
					String source = GameData.COUNTRY_NAMES[owned_interior_territories.get(0)];
					String target = GameData.COUNTRY_NAMES[owned_border_territories.get(i)];

					source = source.replaceAll("\\s", "");
					target = target.replaceAll("\\s", "");

					int armies = board.getNumUnits(owned_interior_territories.get(0)) - 1;

					command = source + " " + target + " " + armies;
				}
			}
		}

		return command;

	}

	private boolean checkI() {
		int[] infantry;
		infantry = new int[] { 0, 0, 0 };

		boolean flagI = player.isCardsAvailable(infantry);
		if (flagI == false) {
			return false;
		} else
			return flagI;
	}

	private boolean checkC() {
		int[] cavalry;
		cavalry = new int[] { 1, 1, 1 };

		boolean flagC = player.isCardsAvailable(cavalry);
		if (flagC == false) {
			return false;
		} else
			return flagC;
	}

	private boolean checkA() {
		int[] art;
		art = new int[] { 2, 2, 2 };

		boolean flagA = player.isCardsAvailable(art);
		if (flagA == false) {
			return false;
		} else
			return flagA;
	}

	private boolean checkFullset() {
		int[] full;
		full = new int[] { 0, 1, 2 };

		boolean flagF = player.isCardsAvailable(full);
		if (flagF == false) {
			return false;
		} else
			return flagF;
	}

	private boolean checkIW() {
		int[] infw1;
		infw1 = new int[] { 0, 0, 3 };

		boolean flagIW = player.isCardsAvailable(infw1);
		if (flagIW == false) {
			return false;
		} else
			return flagIW;
	}

	private boolean checkIWW() {
		int[] infw1;
		infw1 = new int[] { 0, 3, 3 };

		boolean flagIWW = player.isCardsAvailable(infw1);
		if (flagIWW == false) {
			return false;
		} else
			return flagIWW;
	}

	private boolean checkAW() {
		int[] infw1;
		infw1 = new int[] { 2, 2, 3 };

		boolean flagAW = player.isCardsAvailable(infw1);
		if (flagAW == false) {
			return false;
		} else
			return flagAW;
	}

	private boolean checkAWW() {
		int[] infw1;
		infw1 = new int[] { 2, 3, 3 };

		boolean flagAWW = player.isCardsAvailable(infw1);
		if (flagAWW == false) {
			return false;
		} else
			return flagAWW;
	}

	private boolean checkCW() {
		int[] infw1;
		infw1 = new int[] { 1, 1, 3 };

		boolean flagCW = player.isCardsAvailable(infw1);
		if (flagCW == false) {
			return false;
		} else
			return flagCW;
	}

	private boolean checkCWW() {
		int[] infw1;
		infw1 = new int[] { 1, 3, 3 };

		boolean flagCWW = player.isCardsAvailable(infw1);
		if (flagCWW == false) {
			return false;
		} else
			return flagCWW;
	}

	private boolean checkCAW() {
		int[] infw1;
		infw1 = new int[] { 1, 2, 3 };

		boolean flagCAW = player.isCardsAvailable(infw1);
		if (flagCAW == false) {
			return false;
		} else
			return flagCAW;
	}

	private boolean checkIAW() {
		int[] infw1;
		infw1 = new int[] { 0, 2, 3 };

		boolean flagIAW = player.isCardsAvailable(infw1);
		if (flagIAW == false) {
			return false;
		} else
			return flagIAW;
	}

	private boolean checkICW() {
		int[] infw1;
		infw1 = new int[] { 0, 1, 3 };

		boolean flagICW = player.isCardsAvailable(infw1);
		if (flagICW == false) {
			return false;
		} else
			return flagICW;
	}

}
