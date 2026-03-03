

/**
 * 当菜单项ID格式不符合规则时抛出的受检异常。
 * 规则：ID必须符合 "<CATEGORY>-XXX" 格式，其中 CATEGORY 为预定义枚举值，
 * XXX 为三位数字（允许前导零）。
 */
public class InvalidItemIdException extends Exception {
    
    public InvalidItemIdException(String message) {
        super(message);
    }

    public InvalidItemIdException(String message, Throwable cause) {
        super(message, cause);
    }
}