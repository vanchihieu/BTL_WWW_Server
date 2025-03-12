package iuh.fit.se.services;

import iuh.fit.se.dtos.OrderRequest;

public interface VNPayService {
	public String createVNPayUrl(OrderRequest orderRequest);
}
