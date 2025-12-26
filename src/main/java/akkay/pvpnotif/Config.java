package akkay.pvpnotif;

public class Config {
    int threshold = 20;
    boolean showSprintToggled = false;
    Palette palette = Palette.Regular;
    boolean firstLaunch = true;
    boolean soundNotification = true;
    public enum Palette {
        Regular, Trans, Lesbian, Gay
    }
}
