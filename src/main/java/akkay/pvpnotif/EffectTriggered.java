package akkay.pvpnotif;

public class EffectTriggered {
    private boolean effectTriggered = false;
    private int duration;

    public EffectTriggered(int duration) {
        this.duration = duration;
    }

    public boolean isEffectTriggered() {
        return effectTriggered;
    }

    public void setEffectTriggered(boolean flag) {
        this.effectTriggered = flag;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
