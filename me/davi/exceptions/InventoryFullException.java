package me.davi.exceptions;

public class InventoryFullException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InventoryFullException(String message) {
        super(message);
    }
}
