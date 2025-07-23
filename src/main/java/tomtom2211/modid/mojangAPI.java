package tomtom2211.modid;

public class mojangAPI {
    private String name;
    private String id;
    public String getName(){
            return name;
    }
    public String getId(){
        return id;
    }
    public static String formatUuid(String uuid) {
        return uuid.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5"
        );
    }
}
