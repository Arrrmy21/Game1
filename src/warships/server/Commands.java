package warships.server;

public enum Commands {


    SHOOT("Shoot"),
    PUT("Put"),
    START("Start"),
    STARTSOLO("StartSolo"),
    END("End"),
    HELP("Help"),
    EXIT("Exit");

    private final String stringValue;

    Commands(final String text){
        stringValue = text;
    }

    public String toString() {
        return stringValue;
    }
}

