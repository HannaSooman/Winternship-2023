import java.io.*;
import java.util.*;

public class Main {

    //Split line
    public static String[] readLine(String line) {
        String[] actionData = line.split(",", -1);
        return actionData;
    }

    //Check if player exists in recordings to either create a new player or modify an existing one
    public static Player updatePlayerRecordings(String[] action, List<Player> playersList) {
        Player player;
        // --- Check list for empty
        //  --- Check if player is not yet found in list (is new)
        if (getPlayerFromId(action[0], playersList) == null || playersList.isEmpty()) {
            player = new Player(action[0], null, 0, 0, 0, 0);
            playersList.add(player);
        }
        //If player is already recorded
        else player = getPlayerFromId(action[0], playersList);
        return player;
    }

    //Check operation type and make according changes
    public static void processOperation (String[] action, Player player, Host host, List<String[]> actions) throws IOException {
            String operation = action[1];
            String matchId = action[2];
            long currentBalance = player.getBalance();

        // --- Check operation type
        // --- Bet
        //Player cannot bet on a match that they have already bet on
            if (operation.equals("BET") && !playerAlreadyBetOnMatch(player, matchId, actions)) {
                int betAmount = Integer.parseInt(action[3]);
                String[] matchData = getMatchData(matchId);
                long hostBalance = host.getBalance();


                String result = getMatchResult(Objects.requireNonNull(matchData));

                //Check legitimacy
                if (betAmount > currentBalance) {
                    player.setIllegitimateAction(action);
                }
                else {
                    //Calculate winnings/losings if match did not end in a draw
                    if (!result.equals("DRAW")) {
                        String bettingSide = action[4];
                        double rateValue = getRateValue(matchId, bettingSide);
                        int bettingSideAmount = (int) (betAmount * rateValue);

                        //If player bet on winning side
                        //Add betting side amount to current player balance
                        //Subtract betting side amount from current host balance
                        if (bettingSide.equals(result)) {
                            int betsWon = player.getBetsWon();
                            long newPlayerBalance = currentBalance + bettingSideAmount;

                            player.setBetsWon(betsWon + 1);
                            player.setBalance(newPlayerBalance);

                            if (player.getIllegitimateAction() == null){
                                long newHostBalance = hostBalance - bettingSideAmount;
                                long balanceToHost = player.getPlayerBalanceToHost();
                                host.setBalance(newHostBalance);
                                player.setPlayerBalanceToHost(balanceToHost - bettingSideAmount);
                            }
                        }
                        //If player bet on losing side
                        //Subtract bet amount from current player balance
                        //Add bet amount to current host balance
                        else {
                            long newPlayerBalance = currentBalance - betAmount;
                            player.setBalance(newPlayerBalance);

                            if (player.getIllegitimateAction() == null) {
                                long newHostBalance = hostBalance + betAmount;
                                long balanceToHost = player.getPlayerBalanceToHost();
                                player.setPlayerBalanceToHost(balanceToHost + betAmount);
                                host.setBalance(newHostBalance);

                            }
                        }
                    }
                    int totalBets = player.getTotalBets();
                    player.setTotalBets(totalBets + 1);
                }
            }
        // --- Deposit
        else if (operation.equals("DEPOSIT")) {
                int depositAmount = Integer.parseInt(action[3]);

                player.setBalance(currentBalance + depositAmount);
            }
        // --- Withdraw
        else if (operation.equals("WITHDRAW")) {
            int withdrawAmount = Integer.parseInt(action[3]);

            //Check legitimacy
            if (withdrawAmount > currentBalance) {
                player.setIllegitimateAction(action);
            }
            else player.setBalance(currentBalance - withdrawAmount);
        }
    }

    //Check if a person with given id has already bet on a match with given match id
    private static boolean playerAlreadyBetOnMatch(Player checkPlayer, String checkMatchId, List<String[]> actions) {
        for (String[] action : actions) {
            String matchId = action[2];
            String playerId = action[0];
            if (playerId.equals(checkPlayer) && matchId.equals(checkMatchId)){
                return true;
            }
        }
        return false;
    }


    //Find player instance from given id
    public static Player getPlayerFromId(String id, List<Player> playersList) {
        for (Player player : playersList) {
            if (player.getId().equals(id))
                return player;
        }
        return null;
    }

    //Get data for match that has the given id
    public static String[] getMatchData (String searchMatchId) throws IOException{
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream("resources/match_data.txt")))) {
            while (true) {
                String match = fileReader.readLine();
                if (match == null)
                    break;
                String[] matchData = match.split(",", -1);
                String matchId = matchData[0];
                // Find data for the match with given ID
                if (searchMatchId.equals(matchId))
                    return matchData;
            }
        }
        return null;
    }

    //Return winning side of match
    public static String getMatchResult(String[] matchData) {
       String matchResult = matchData[3];
       return matchResult;
    }

    //Search rate value of given side during the wanted match
    public static double getRateValue(String searchId, String result) throws IOException {
        String[] matchData = getMatchData(searchId);
        String matchId = matchData[0];
        double coefficientA = Double.parseDouble(matchData[1]);
        double coefficientB = Double.parseDouble(matchData[2]);

        if (matchId.equals(searchId)) {
            if (result.equals("A"))
                return coefficientA;
            else return coefficientB;
        }
        return 0;
    }

    public static void main(String[] args) throws IOException {

        // --- Initiating host with balance of 0
        Host host = new Host(0);

        // --- Recording all players
        List<Player> playersList = new ArrayList<>();

        // --- Recording all actions
        List<String[]> actions = new ArrayList<>();

        // --- Reading player_data line by line
        FileReader reader = new FileReader("resources/player_data.txt");
        try (BufferedReader fileReader = new BufferedReader(reader)) {
            String line;
            while ((line = fileReader.readLine()) != null) {
                // --- Processing each line as operation
                String[] action = readLine(line);
                actions.add(action);
                Player player = updatePlayerRecordings(action, playersList);
                processOperation(action, player, host, actions);
            }
            reader.close();
        }

        // --- Dividing players by illegitimate action history
        List<Player> legitimatePlayers = new ArrayList<>();
        List<Player> illegitimatePlayers = new ArrayList<>();

        for (Player player : playersList) {
            //When player does not have an action recorded as illegitimate then they are added to legitimatePlayers list
            if (player.getIllegitimateAction() == null){
                legitimatePlayers.add(player);
            }
            else {
                //When player has an action recorded as illegitimate then they are added to illegitimatePlayers list
                illegitimatePlayers.add(player);

                //Remove each illegitimate player's impact to host balance
                long hostBalance = host.getBalance();
                host.setBalance(hostBalance - player.getPlayerBalanceToHost());

            }
        }

        // --- Writing as per format
        try{
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter("src/result.txt"));

            //Write empty line if there are no legitimate players
            if (legitimatePlayers.isEmpty()) {
                fileWriter.newLine();
            }
            else {
                //Iterate through legitimate players and write to file
                for (Player legitimatePlayer : legitimatePlayers) {
                    fileWriter.write(legitimatePlayer.toString());
                    fileWriter.newLine();

                }
            }
            fileWriter.newLine();

            //Write empty line if there are no illegitimate players
            if (illegitimatePlayers.isEmpty()) {
                fileWriter.newLine();
            }
            else {
                //Iterate through illegitimate players and write to file
                for (Player illegitimatePlayer : illegitimatePlayers) {
                    StringBuilder text = new StringBuilder();

                    for (Object illegitimateValue : illegitimatePlayer.getIllegitimateAction()) {
                        if (illegitimateValue == "")
                            illegitimateValue = null;
                        text.append(illegitimateValue).append(" ");
                    }
                    fileWriter.write(String.valueOf(text));
                    fileWriter.newLine();
                }
            }
            fileWriter.newLine();

            fileWriter.write(host.toString());

            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


