package signups;

/**
 * A class that can hold information about a player.
 * @author Eren Bole.8720
 * @version 1.0
 */
public class Player {

    private final String gw2Account;
    private final String discordName;
    private final String tier;
    private final String comments;
    private final int roles;
    private String assignedRole;

    public Player(String gw2Account, String discordName, String tier, String comments, int roles) {
        this.gw2Account = gw2Account;
        this.discordName = discordName;
        this.tier = tier;
        this.comments = comments;
        this.roles = roles;
    }

    public String toString() {
        return String.format("%s - %s", gw2Account, assignedRole);
    }

    public String getGw2Account() {
        return gw2Account;
    }

    public String getDiscordName() {
        return discordName;
    }

    public String getTier() {
        return tier;
    }

    public String getComments() {
        return comments;
    }

    public int getRoles() {
        return roles;
    }

    public void setAssignedRole(int role) {
        this.assignedRole = getRole(role);
    }

    public String getAssignedRole() {
        return assignedRole;
    }

    /**
     * Translate an integer role value into it's role name.
     * @param role The role value.
     * @return The name of the role.
     */
    private String getRole(int role) {
        switch (role) {
            case 1:
            case 2:
            case 3:
                return "DPS";
            case 4:
                return "Banners";
            case 8:
                return "Offheal";
            case 16:
                return "Heal Renegade";
            case 32:
                return "Heal FB";
            case 64:
                return "Druid";
            case 128:
                return "Alacrigade";
            case 256:
                return "Quickness FB";
            case 512:
                return "Power Boon Chrono";
            case 1024:
                return "Chrono Tank";
            default:
                return "Other";
        }
    }
}
