package mathtrainer;

public class Teams {

    protected static final short MICHAEL = 0;
    protected static final short MAGDALENA = 1;
    protected static final short HANNAH = 2;
    protected static final short MARIA = 3;
    protected static final short TIBOR = 4;
    protected static final short FIVEA = 5;
    protected static final short FIVEB = 6;
    protected static final short SIXB = 7;

    private static final String[] TEAM_NAMES = {
            "Michael",
            "Magdalena",
            "9b 2025/2026",
            "9a 2025/2026",
            "Tibor",
            "5a 2025/2026",
            "Laura und Michel",
            "6b 2025/2026"
    };

    protected static String getTeamName(int id) {
        if (id >= 0 && id < TEAM_NAMES.length) {
            return "Team " + TEAM_NAMES[id];
        }
        return "Unknown";
    }
}