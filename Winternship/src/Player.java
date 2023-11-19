public class Player {
    private String id;
    private String[] illegitimateAction;
    private long balance;
    private int totalBets;
    private int betsWon;
    private long playerBalanceToHost;

    public Player(String id, String[] illegitimateAction, long balance, int totalBets, int betsWon, long playerBalanceToHost) {
        this.id = id;
        this.illegitimateAction = illegitimateAction;
        this.balance = balance;
        this.totalBets = totalBets;
        this.betsWon = betsWon;
        this.playerBalanceToHost = playerBalanceToHost;
    }

    public String getId() {
        return id;
    }

    public String[] getIllegitimateAction() {
        return illegitimateAction;
    }

    public void setIllegitimateAction(String[] illegitimateAction) {
        this.illegitimateAction = illegitimateAction;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public int getTotalBets() {
        return totalBets;
    }

    public void setTotalBets(int totalBets) {
        this.totalBets = totalBets;
    }

    public int getBetsWon() {
        return betsWon;
    }

    public void setBetsWon(int betsWon) {
        this.betsWon = betsWon;
    }

    public long getPlayerBalanceToHost() {
        return playerBalanceToHost;
    }

    public void setPlayerBalanceToHost(long playerBalanceToHost) {
        this.playerBalanceToHost = playerBalanceToHost;
    }

    @Override
    public String toString() {
        double ratio = (double)betsWon/totalBets;
        double scale = Math.pow(10, 2);
        double decimals = Math.round(ratio * scale) / scale;
        return id + " " + balance + " " + decimals;
    }
}

