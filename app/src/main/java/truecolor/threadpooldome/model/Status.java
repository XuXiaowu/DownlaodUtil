package truecolor.threadpooldome.model;

/**
 * Created by xiaowu on 15/5/27.
 */
public enum Status {
    WATING, STARTED, LOADING, SUCCESS, PAUSE, FAILE;

    public static String valueOf(int value){
        switch (value){
            case 0:
                return WATING.toString();
            case 1:
                return STARTED.toString();
            case 2:
                return LOADING.toString();
            case 3:
                return SUCCESS.toString();
            case 4:
                return PAUSE.toString();
            case 5:
                return FAILE.toString();
            default:
                return null;
        }
    }

    public static int valueOf(Status status){
        switch (status){
            case WATING:
                return 0;
            case STARTED:
                return 1;
            case LOADING:
                return 2;
            case SUCCESS:
                return 3;
            case PAUSE:
                return 4;
            case FAILE:
                return 5;
            default:
                return -1;
        }
    }
}
