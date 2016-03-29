
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/* Algorithmics
 * 14708689 Orla Cullen
 * 05641349 Gavin Keaveney
 * 14343826 Jonathan Sweeney 
 */
public class PlayGame {

	//arraylist represents the random ownership of the cards of a shuffled deck.
	List<Integer> arrayList = deal();
	
	//Creates the territories, assigning them the owners as determined in arraylist.
	List<Territory> territory_list = buildTerritories();
	
	//Creates the Game Board, to show all game developments.
	MapPanel mapPanel = new MapPanel(territory_list);
	
	//Creates the a Jframe split into 3 panels, one of which will hold the map.
	SplitFrameGUI interfaceFrame = new SplitFrameGUI(mapPanel);

	
	PlayGame(){
		
		
		
		interfaceFrame.pack();
		interfaceFrame.setVisible(true);
		
		//Gets player names from prompt
		String player_1 = getNames(interfaceFrame, 1);
		String player_2 = getNames(interfaceFrame, 2);
		
		//Creates list of players and syncs each one with their owned territories.
		List<Player> player_list = buildPlayers(territory_list, player_1, player_2);
	
		
		//Draws territory cards and displays them to users.
		draw(territory_list, player_list, arrayList);
		
		interfaceFrame.displayString("Enter 'roll' to decide who places armies first.");
		int winner = roll();
		interfaceFrame.displayString(player_list.get(winner).getName() +" will place armies first.");
		
		//Method to allow players to set up the board, each player placing 27 armies on their own territories and 9 on each neutral.
		placeArmies(winner, territory_list, player_list);
		
		interfaceFrame.displayString("Enter 'roll' to decide who goes first.");
		winner = roll();
		interfaceFrame.displayString(player_list.get(winner).getName() +" has the first turn.");
		
		while(true){
			int current_player = 0;
			for(int i=0;i<2;i++){
				
				current_player=0;
				
				if (i % 2 == 0){
					current_player = winner;
				}
				else{
					current_player = (winner + 1) % 2;
				}
				
				check_HumanWinner(player_list);
				turn(current_player, territory_list, player_list);
			}
		}
	}
	
	public void turn(int current_player, List<Territory> territory_list, List<Player> player_list){
		
		reinforceTerritories(current_player, territory_list, player_list);
		combat(current_player, territory_list, player_list);
		fortify(current_player, territory_list, player_list);
		interfaceFrame.displayString("End of " + player_list.get(current_player).getName() + "'s turn.");	
		interfaceFrame.displayString("Beginning " + player_list.get((current_player) % 2).getName() + "'s turn.");	
	}

	
	public void reinforceTerritories(int current_player, List<Territory> territory_list, List<Player> player_list){
		int reinforcements = calc_TotalReinforcements(territory_list, player_list, current_player);
		player_list.get(current_player).setArmies(reinforcements);
		
		while(player_list.get(current_player).getArmies() > 0){
			interfaceFrame.displayString(player_list.get(current_player).getName() + ", please choose one of your territories to place armies on.");
			assignArmies(territory_list, player_list, current_player, 1);
		}	
	}
	
	public void combat(int current_player, List<Territory> territory_list, List<Player> player_list){
		
		interfaceFrame.displayString(player_list.get(current_player).getName() + ", please choose one of your territories to launch an attack or enter 'skip' to end combat.");	
		do{	
			boolean valid_choice = false;
			int chosen_node = getCombatInput(territory_list);
			if(chosen_node == -2){
				break;
			}
			for (int j=0; j < player_list.get(current_player).ownedTerritoriesSize() ; j++){
				if(chosen_node == player_list.get(current_player).getOwnedTerritory(j)){
					if(territory_list.get(chosen_node).getArmies() == 1){
						interfaceFrame.displayString("You cannot attack from a territory with 1 army");	
						valid_choice = true;
						break;
					}
					battle(chosen_node, current_player, territory_list, player_list);
					mapPanel.refresh();
					valid_choice = true;
					break;
				}
			}
			
			if(valid_choice==true){
				interfaceFrame.displayString(player_list.get(current_player).getName() + ", please choose one of your territories to launch an attack or enter 'skip' to end combat.");
				continue;
			}
			
			interfaceFrame.displayString(player_list.get(current_player).getName() + " does not own " + GameData.COUNTRY_NAMES[chosen_node]);
			interfaceFrame.displayString("Please enter a territory owned by " + player_list.get(current_player).getName());
		}while(true);
		
		interfaceFrame.displayString(player_list.get(current_player).getName() + " has ended combat.");
	}
	
	public int getCombatInput(List<Territory> territory_list){
		int chosen_node = -1;
		do{	
			String loop = interfaceFrame.getCommand();
			
			if(loop.equalsIgnoreCase("skip")){
				chosen_node = -2;
				break;
			}
			
			for(int i=0; i<42; i++){
				if (loop.equalsIgnoreCase(GameData.COUNTRY_NAMES[i]) || loop.equalsIgnoreCase(GameData.SHORT_COUNTRY_NAMES[i])){
					chosen_node = territory_list.get(i).getNode();
					break;
				}
			}
			
			if(chosen_node == -1){
				interfaceFrame.displayString("NAME NOT RECOGNISED");
				interfaceFrame.displayString("Please enter a valid name or shorthand. ");
			}

		}while(chosen_node == -1);
		
		return chosen_node;
	}
	
	public void battle(int chosen_node, int current_player, List<Territory> territory_list, List<Player> player_list){
		boolean attack_again = false;
		interfaceFrame.displayString("Please choose a bordering opponant's territory to attack");	
		do{
			boolean valid_choice = true;
			boolean valid_target = false;
			int chosen_target = getCombatInput(territory_list);
			if(chosen_node == -2){
				break;
			}
			
			for (int j=0; j < player_list.get(current_player).ownedTerritoriesSize() ; j++){
				if(chosen_target == player_list.get(current_player).getOwnedTerritory(j)){
					interfaceFrame.displayString("You cannot attack your own territory");	
					valid_choice=false;
					break;
				}
			}
			
			if(valid_choice==false){
				interfaceFrame.displayString("Please choose a bordering opponant's territory to attack");
				continue;
			}
			
			for(int j=0;j<6;j++){	
					try{
						if(GameData.ADJACENT[chosen_node][j] == chosen_target){
							valid_target = true;
							break;
						}
					}
					catch(ArrayIndexOutOfBoundsException e){
						break;
					}
				}
			
			if(valid_target==false){
				interfaceFrame.displayString(GameData.COUNTRY_NAMES[chosen_target] + " is not adjacent to " + GameData.COUNTRY_NAMES[chosen_node]);
				interfaceFrame.displayString("Please choose a bordering opponant's territory to attack");
				continue;
			}
		
			
			do{
				interfaceFrame.displayString(player_list.get(current_player).getName() + ", with how many armies do you want to attack " + GameData.COUNTRY_NAMES[chosen_target]);	
				int attack_number;
				int defend_number;
				do {	
					String loop = interfaceFrame.getCommand();
					
					try{
						attack_number = Integer.parseInt(loop);
					}
					catch(Exception e){
						interfaceFrame.displayString("You must enter an integer value.");
						continue;
					}
					
					if (attack_number >= territory_list.get(chosen_node).getArmies() || attack_number <= 0){
						interfaceFrame.displayString("You cannot attack with that many armies.");
						continue;
					}
						
					break;
				} while(true);
					
				interfaceFrame.displayString(player_list.get((current_player + 1) % 2).getName() + ", would you like to defend " + GameData.COUNTRY_NAMES[chosen_target] + " with 1 or 2 armies?");	
				do {	
					String loop = interfaceFrame.getCommand();
					
					try{
						defend_number = Integer.parseInt(loop);
					}
					catch(Exception e){
						interfaceFrame.displayString("You must enter an integer value.");
						continue;
					}
				
					if (defend_number > territory_list.get(chosen_target).getArmies() || defend_number <= 0 || defend_number > 2){
						interfaceFrame.displayString("You cannot attack with that many armies.");
						continue;
					}
						
					break;
				} while(true);
					
				combatRoll(current_player, attack_number, defend_number, chosen_node, chosen_target, territory_list, player_list);
				if(territory_list.get(chosen_target).getArmies()==0){
					interfaceFrame.displayString(player_list.get(current_player).getName() + " has captured " + GameData.COUNTRY_NAMES[chosen_target]);	
					
					player_list.get(territory_list.get(chosen_target).getPlayer()).removeOwnedTerritory(chosen_target);
					player_list.get(current_player).addOwnedTerritory(chosen_target);
					territory_list.get(chosen_target).setPlayer(current_player);
					territory_list.get(chosen_target).setArmies(1);
					territory_list.get(chosen_node).setArmies(-1);
					mapPanel.refresh();
					removePlayer(player_list);
					check_HumanWinner(player_list);
					attack_again = false;
					break;
				}
				else{
					interfaceFrame.displayString(player_list.get(current_player).getName() + ", would you like to attack " + GameData.COUNTRY_NAMES[chosen_target] + " again? Y/N");	
					do {	
						String word = interfaceFrame.getCommand();
						if (word.equalsIgnoreCase("Y")){
							attack_again = true;
							break;
						}
						else if (word.equalsIgnoreCase("N")){
							attack_again = false;
							break;
						}
						else{
							interfaceFrame.displayString("COMMAND NOT RECOGNISED");
							interfaceFrame.displayString("Please enter Y or N. ");
						}
					} while(true);
				}	
			}while(attack_again);	
		}while(attack_again);
	}
	
	public void combatRoll(int current_player, int attack_number, int defend_number, int chosen_node, int chosen_target, List<Territory> territory_list, List<Player> player_list){
		List<Integer> attacking_rolls = new ArrayList<Integer>();
		List<Integer> defending_rolls = new ArrayList<Integer>();
		
		interfaceFrame.displayString(player_list.get(current_player).getName() + " rolls:");
		for(int j=0; j<attack_number; j++){
			Die die = new Die();
			die.roll();
			attacking_rolls.add(die.value());
			interfaceFrame.displayString("" + die.value());
		}
		
		interfaceFrame.displayString(player_list.get((current_player + 1) % 2).getName() + " rolls:");
		for(int j=0; j<defend_number; j++){
			Die die = new Die();
			die.roll();
			defending_rolls.add(die.value());
			interfaceFrame.displayString("" + die.value());
		}
		
		Collections.sort(attacking_rolls);
		Collections.sort(defending_rolls);
		Collections.reverse(attacking_rolls);
		Collections.reverse(defending_rolls);
		
		if(attacking_rolls.get(0) > defending_rolls.get(0)){
			interfaceFrame.displayString(player_list.get(current_player).getName() + " wins a battle, defending player loses one army.");
			territory_list.get(chosen_target).setArmies(-1);
		}
		else{
			interfaceFrame.displayString(player_list.get(current_player).getName() + " loses a battle, attacking player loses one army.");	
			territory_list.get(chosen_node).setArmies(-1);
		}
		mapPanel.refresh();
		if(defend_number==2 && attack_number>=2){
			if(attacking_rolls.get(1) > defending_rolls.get(1)){
				interfaceFrame.displayString(player_list.get(current_player).getName() + " wins a battle, defending player loses one army.");
				territory_list.get(chosen_target).setArmies(-1);
			}
			else{
				interfaceFrame.displayString(player_list.get(current_player).getName() + " loses a battle, attacking player loses one army.");	
				territory_list.get(chosen_node).setArmies(-1);
			}
			mapPanel.refresh();
		}
	}
	
	public void fortify(int current_player, List<Territory> territory_list, List<Player> player_list){
		interfaceFrame.displayString(player_list.get(current_player).getName() + ", please choose one of your territories to move armies from or enter 'skip'.");	
		boolean valid_choice = false;
		do{
			boolean repeat = false;
			boolean success = false;
			int chosen_target = 0;
			int chosen_node = getCombatInput(territory_list);
			if(chosen_node == -2){
				break;
			}
			for (int j=0; j < player_list.get(current_player).ownedTerritoriesSize() ; j++){
				if(chosen_node == player_list.get(current_player).getOwnedTerritory(j)){
					if(territory_list.get(chosen_node).getArmies() == 1){
						interfaceFrame.displayString("You cannot move armies from a territory with 1 army");	
						repeat = true;
						break;
					}
					interfaceFrame.displayString(player_list.get(current_player).getName() + ", please choose one of your territories fortify or enter 'skip'.");	
					
					do{
						chosen_target = getCombatInput(territory_list);
						if(chosen_target == -2){
							break;
						}
						for (int i=0; i < player_list.get(current_player).ownedTerritoriesSize() ; i++){
							if(chosen_node == player_list.get(current_player).getOwnedTerritory(i)){
								success = moveArmies(chosen_node, chosen_target, current_player, territory_list, player_list);
								mapPanel.refresh();
								break;
							}
						}
						
						if(success==true){
							break;
						}
						
						interfaceFrame.displayString(player_list.get(current_player).getName() + " does not own " + GameData.COUNTRY_NAMES[chosen_target]);
						interfaceFrame.displayString("Please enter a territory owned by " + player_list.get(current_player).getName());	
					}while(true);
					mapPanel.refresh();
					valid_choice = true;
					break;
				}
			}
			
			if(chosen_target == -2){
				break;
			}
			
			if(repeat = true){
				interfaceFrame.displayString(player_list.get(current_player).getName() + ", please choose one of your territories to move armies from or enter 'skip'.");	
				continue;
			}
			
			if(valid_choice==true){
				break;
			}
			
			interfaceFrame.displayString(player_list.get(current_player).getName() + " does not own " + GameData.COUNTRY_NAMES[chosen_node]);
			interfaceFrame.displayString("Please enter a territory owned by " + player_list.get(current_player).getName());	
		}while(true);
	}
	
	public boolean moveArmies(int chosen_node, int chosen_target, int current_player, List<Territory> territory_list, List<Player> player_list){
		interfaceFrame.displayString(player_list.get(current_player).getName() + ", how many armies would you like to move from " 
										+ GameData.COUNTRY_NAMES[chosen_node] + " to " + GameData.COUNTRY_NAMES[chosen_target] + "?");
			int move_number;
			boolean success = false;
			do {	
				String loop = interfaceFrame.getCommand();
				
				try{
					move_number = Integer.parseInt(loop);
				}
				catch(Exception e){
					interfaceFrame.displayString("You must enter an integer value.");
					continue;
				}
				
				if (move_number >= territory_list.get(chosen_node).getArmies() || move_number <= 0){
					interfaceFrame.displayString("You cannot move that many armies.");
					continue;
				}
					
				break;
			}while(true);
			
			boolean valid_target = false;
			for(int j=0;j<6;j++){	
				try{
					if(GameData.ADJACENT[chosen_node][j] == chosen_target){
						valid_target = true;
						break;
					}
				}
				catch(ArrayIndexOutOfBoundsException e){
					break;
				}
			}
		
		if(valid_target==false){
			interfaceFrame.displayString(GameData.COUNTRY_NAMES[chosen_target] + " is not adjacent to " + GameData.COUNTRY_NAMES[chosen_node]);
			interfaceFrame.displayString("Please choose a bordering territory to fortify");
			success = false;
		}
		
		else{
			territory_list.get(chosen_target).setArmies(move_number);
			territory_list.get(chosen_node).setArmies(-move_number);
			mapPanel.refresh();
			success = true;
		}
		
		return success;
	}
	
	public void draw(List<Territory> territory_list, List<Player> player_list, List<Integer> arrayList){
		
		do {
			interfaceFrame.displayString("Enter 'draw' to draw territory cards");
			
			String loop = interfaceFrame.getCommand();
			if (loop.equalsIgnoreCase("draw")){
				break;
			}
			else if (!(loop.equalsIgnoreCase("draw"))){
				interfaceFrame.displayString("COMMAND NOT RECOGNISED");
				interfaceFrame.displayString("Would you like to draw territory cards? ");
				interfaceFrame.displayString("Enter Y for Yes or N for No ");
				String word = interfaceFrame.getCommand();
				
				if (word.equalsIgnoreCase("Y")){
					break;
				}
				else if (word.equalsIgnoreCase("N")){
					interfaceFrame.displayString("Cannot continue, thank you for playing. ");
					try {
						TimeUnit.SECONDS.sleep(2);
					} 
					catch (InterruptedException e){
					}
					System.exit(0);
				}
			}
		} while (true);
		assignTerritories(territory_list, player_list, arrayList);
		printNames(player_list);
	}
	
	//
	public int roll(){
		int winner;
		do {
			String loop = interfaceFrame.getCommand();
			if (loop.equalsIgnoreCase("roll")){
				winner = rollDice();
				break;
			}
			else if (!(loop.equalsIgnoreCase("roll"))){
				interfaceFrame.displayString("COMMAND NOT RECOGNISED");
				interfaceFrame.displayString("Would you like to roll dice? ");
				interfaceFrame.displayString("Enter Y for Yes or N for No ");
				String word = interfaceFrame.getCommand();
				
				if (word.equalsIgnoreCase("Y")){
					winner = rollDice();
					break;
				}
				else if (word.equalsIgnoreCase("N")){
					interfaceFrame.displayString("Cannot continue, thank you for playing. ");
					try {
						TimeUnit.SECONDS.sleep(2);
					} 
					catch (InterruptedException e){
					}
					System.exit(0);
				}
			}
		} while (true);
	
		return winner;
	}

	
	//Lets both players set up the board by allocating the armies to their chosen territories.
	public void placeArmies(int winner, List<Territory> territory_list, List<Player> player_list) {
		int current_player =0;
		int i = 0;
		for(i=0;i<18;i++) { // **** Keep at i<18 (Change to 2 for quick start) ****
			current_player=0;
			
			if (i % 2 == 0){
				current_player = winner;
			}
				else{
					current_player = (winner + 1) % 2;
				}
			for(int j=0; j<3; j++){
				interfaceFrame.displayString(player_list.get(current_player).getName() + ", please choose one of your territories to place  armies on.");
				assignArmies(territory_list, player_list, current_player, 1);
			}
					
			for (int k = 2; k < 6; k++) {
				interfaceFrame.displayString(
						player_list.get(current_player).getName() + ", please choose one of Neutral Player" + (k-1) + " territories" + "(" + GameData.PLAYER_COLOURS[k] + ")" + "to place 1 army on.");
				assignArmies(territory_list, player_list, k, 1);
			}
		}
}
	
	//Assigns armies to a chosen territory belonging to a given player.
	public void assignArmies(List<Territory> territory_list, List<Player> player_list, int player, int armies){
		do{	
			boolean valid_choice = false;
			int chosen_node = getTerritoryInput(territory_list);
			for (int j=0; j < player_list.get(player).ownedTerritoriesSize() ; j++){
				if(chosen_node == player_list.get(player).getOwnedTerritory(j)){
					player_list.get(player).setArmies(-armies);
					territory_list.get(chosen_node).setArmies(armies);
					mapPanel.refresh();
					valid_choice = true;
					break;
				}
			}
			if(valid_choice==true){
				break;
			}
			interfaceFrame.displayString(player_list.get(player).getName() + " does not own " + GameData.COUNTRY_NAMES[chosen_node]);
			interfaceFrame.displayString("Please enter a territory owned by " + player_list.get(player).getName());
		}while(true);
		
	}
	
	
	//Reads the entered territory name from the prompt.
	public int getTerritoryInput(List<Territory> territory_list){
		int chosen_node = -1;
		do {	
			String loop = interfaceFrame.getCommand();
			for(int i=0; i<42; i++){
				if (loop.equalsIgnoreCase(GameData.COUNTRY_NAMES[i]) || loop.equalsIgnoreCase(GameData.SHORT_COUNTRY_NAMES[i])){
					chosen_node = territory_list.get(i).getNode();
					break;
				}
			}
			if(chosen_node == -1){
				interfaceFrame.displayString("NAME NOT RECOGNISED");
				interfaceFrame.displayString("Please enter a valid name or shorthand. ");
			}

		} while (chosen_node == -1);
		
		return chosen_node;
	}
	
	
	//Rolls the dice and returns the winner when called.
	public int rollDice() {
		Die die = new Die();
		die.roll();
		int die1 = die.value();
		interfaceFrame.displayString(" Player 1 rolled: " + die.getDie());
		die.roll();
		int die2 = die.value();
		interfaceFrame.displayString(" Player 2 rolled: " + die.getDie());

		int winner = 0;
		if (die1 > die2) {
			winner = 0;
			interfaceFrame.displayString(" Player 1  wins");
		} else if (die1 < die2) {
			winner = 1;

			interfaceFrame.displayString(" Player 2  wins");
		} else{
			interfaceFrame.displayString(" Draw, Re-Rolling");
			try {
				TimeUnit.SECONDS.sleep(1);
			} 
			catch (InterruptedException e){
			}
			winner = rollDice();
		}
		return winner;
	}
	
	
	//Creates and initializes the list of territories.
	public List<Territory> buildTerritories(){
		List<Territory> territory_list= new ArrayList<Territory>();
		
		for(int i=0;i<42;i++){
			Territory current_territory = new Territory(i, GameData.COUNTRY_NAMES[i], GameData.SHORT_COUNTRY_NAMES[i]);
			current_territory.setArmies(1);
		
			current_territory.setPlayer(-1);
				
	        territory_list.add(current_territory);
		}
		 return territory_list;
	}
	
	
	//Creates and initializes the list of 6 players.
	public List<Player> buildPlayers(List<Territory> territory_list,  String player_1, String player_2){	
		List<Player> player_list= new ArrayList<Player>();
		String player_name = null;
		int armies = 0;
		for(int i=0;i<6;i++){
			switch (i) {
			case 0:  player_name = player_1;
					 armies = 27; // **** Keep at 27 (Change to 3 for quick start) ****
				break;
			case 1:  player_name = player_2;
           		break;
			case 2:  player_name = "Neutral Player 1";
					 armies = 18;
            	break;
			case 3:  player_name = "Neutral Player 2";
				break;
			case 4:  player_name = "Neutral Player 3";
				break;
			case 5:  player_name = "Neutral Player 4";
				break;
			}	
			Player current_player = new Player(i, player_name);
			current_player.setArmies(armies);

					

			player_list.add(current_player);
		}
		 return player_list;
	}
	
	
	public void assignTerritories(List<Territory> territory_list, List<Player> player_list, List<Integer> arrayList){
		for(int i=0;i<42;i++){
			territory_list.get(i).setPlayer(arrayList.get(i));
		}
		for(int i=0;i<6;i++){
			for(int j=0;j<	42 ;j++){
				if(player_list.get(i).getPlayer()==territory_list.get(j).getPlayer()){
					player_list.get(i).addOwnedTerritory(territory_list.get(j).getNode());
				}
			}
		}
		mapPanel.refresh();
	}
	
	
	//Creates a length 42 integer list of numbers 0-5 and randomizes it.
	public List<Integer> deal() {
		int i=0;
		int current_player = 0;
		List<Integer> arrayList = new ArrayList<Integer>();
		
		for (i=0;i<	42 ;i++){
			switch (i) {
			case 0:  current_player = 0;
	        		 break;
	        case 9:  current_player = 1;
	                 break;
	        case 18: current_player = 2;
	        		 break;
	        case 24: current_player = 3;
	        		 break;
	        case 30: current_player = 4;
	                 break;
	        case 36: current_player = 5; 
	                 break;
			}
			arrayList.add(current_player);
		}

		Collections.shuffle(arrayList);
		
		return arrayList;
	}
	
	
	//Get names from prompt.
	public  String getNames(SplitFrameGUI interfaceFrame, int player_number){
		interfaceFrame.displayString("Enter the name of player " + player_number);
		String name = interfaceFrame.getCommand();
		interfaceFrame.displayString("Welcome to risk " + name);
		
		return name;
	}

	
	//Print each player's names and owned territories.
	public void printNames(List<Player> player_list){
		for(int j=0; j<6;j++){
			String nameList = "";
			for (int i=0; i < player_list.get(j).ownedTerritoriesSize() ; i++){
				nameList += (GameData.COUNTRY_NAMES[player_list.get(j).getOwnedTerritory(i)] + ", ");
			}
			interfaceFrame.displayString(player_list.get(j).getName() + " (" + GameData.PLAYER_COLOURS[j] + ")" +" has received " + nameList + "\n");
		}
	}
	
	//Method  calculates the  amount of territories owned   and  also gives a bonus if continent is owned 
		public int  calc_TotalReinforcements(List<Territory> territory_list, List<Player> player_list, int i) {
				int Namerica_size = 0;
				int Euro_size = 0;
				int Asia_size = 0;
				int Aus_size = 0;
				int Samerica_size = 0;
				int Af_size = 0;
				int nam_reinforce = 0;
				int eu_reinforce = 0;
				int as_reinforce = 0;
				int aus_reinforce = 0;
				int sam_reinforce = 0;
				int af_reinforce=0;
				int country_reinforce = 0;
				if (player_list.get(i).ownedTerritoriesSize() / 3 <= 3) {
					country_reinforce = 3;
					} 
				else if (player_list.get(i).ownedTerritoriesSize() / 3 > 3) {
					country_reinforce = player_list.get(i).ownedTerritoriesSize() / 3;
				}
				for (int j = 0; j < 42; j++) {
					
					if (j < 9 && (player_list.get(i).getPlayer(i) == territory_list.get(j).getPlayer())) {
						Namerica_size++;
						if (Namerica_size==9){
						nam_reinforce = GameData.north_america;
						}
						else{
							nam_reinforce = 0;
						}
					}
					if (j > 8 && j < 16 && (player_list.get(i).getPlayer(i) == territory_list.get(j).getPlayer())) {
						Euro_size++;
						if (Euro_size==7){
							eu_reinforce = GameData.europe;
							}
							else{
								eu_reinforce = 0;
							}
						

					}
					if (j > 15 && j < 28 && (player_list.get(i).getPlayer(i) == territory_list.get(j).getPlayer())) {
						Asia_size++;
						if (Asia_size==12){
							as_reinforce = GameData.asia;
							}
							else{
								as_reinforce = 0;
							}
						
					}
					if (j > 27 && j < 32 && (player_list.get(i).getPlayer(i) == territory_list.get(j).getPlayer())) {
						Aus_size++;
						if (Aus_size==4){
							aus_reinforce =  GameData.australia;
							}
							else{
								aus_reinforce = 0;
							}
						
					}
					if (j > 31 && j < 36 && (player_list.get(i).getPlayer(i) == territory_list.get(j).getPlayer())) {
						Samerica_size++;
						if (Samerica_size==4){
							sam_reinforce =  GameData.south_america;
							}
							else{
								sam_reinforce = 0;
							}
						

					}
					if (j > 35 && j < 42 && (player_list.get(i).getPlayer(i) == territory_list.get(j).getPlayer())) {
						Af_size++;
						if (Af_size==6){
							af_reinforce =  GameData.africa;
							}
							else{
								af_reinforce = 0;
							}
						
					}
					
				}
				int continent_reinforce = 0;
				continent_reinforce= nam_reinforce+eu_reinforce + as_reinforce + aus_reinforce +sam_reinforce+ af_reinforce;
				int total_reinforcements=0;
				 total_reinforcements=continent_reinforce+ country_reinforce;
				//interfaceFrame.displayString(player_list.get(i).getName() + ", has " + Namerica_size + " size of america");
				//interfaceFrame.displayString(player_list.get(i).getName() + ", has " + Euro_size + " size of europe");
				//interfaceFrame.displayString(player_list.get(i).getName() + ", has " + Asia_size + " size of asia");
				//interfaceFrame.displayString(player_list.get(i).getName() + ", has " + Aus_size + " size of austalia");
				//interfaceFrame.displayString(player_list.get(i).getName() + ", has " + Samerica_size + " size of Southamerica");
				//interfaceFrame.displayString(player_list.get(i).getName() + ", has " + Af_size + " size of Africa");
				interfaceFrame.displayString(player_list.get(i).getName() + ", has " + continent_reinforce + " bonus reinforcements.");
				interfaceFrame.displayString(player_list.get(i).getName() + ", has " + total_reinforcements + " total reinforcements.");
			
		return total_reinforcements;}
		
		public  void check_HumanWinner(List<Player> player_list){
			for (int i=0;i<GameData.NUM_PLAYERS;i++){
				if (i==0 && player_list.get(i).ownedTerritoriesSize()==0){
					interfaceFrame.displayString(player_list.get(i+1).getName() + " is the winner!");
					interfaceFrame.displayString( "GAME OVER");
					try {
						TimeUnit.SECONDS.sleep(5);
					} 
					catch (InterruptedException e){
					}
					System.exit(0);
					
				}
				if (i==1 && player_list.get(i).ownedTerritoriesSize()==0){
					interfaceFrame.displayString(player_list.get(i-1).getName() + " is the winner!");
					interfaceFrame.displayString( "GAME OVER");
					try {
						TimeUnit.SECONDS.sleep(5);
					} 
					catch (InterruptedException e){
					}
					System.exit(0);
				}
			}
		}
		
		public void removePlayer(List<Player> player_list){
			 try{		
				for(int i=2;i<GameData.NUM_PLAYERS_PLUS_NEUTRALS;i++){
			 		if(player_list.get(i).ownedTerritoriesSize()==0){
			 			interfaceFrame.displayString(player_list.get(i).getName() + " has been eliminated"); 
			 			break;
			 		}				
			 	}
			 }
			catch(ArrayIndexOutOfBoundsException e){
			} 			
		}
}




