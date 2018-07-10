package com.imooc.myo2o.exceptions;
/**
 * 操作店铺相关的异常
 * @author Administrator
 *
 */
public class ShopOperationException extends RuntimeException {

	private static final long serialVersionUID = -7010756506006861443L;

	public ShopOperationException(String msg) {
		super(msg);
	}
  
}
