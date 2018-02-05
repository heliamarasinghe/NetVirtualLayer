package com.imaginelab.sdn_icf.deploy;

import java.util.Comparator;

import com.imaginelab.sdn_icf.containers.AcceptedReq;


public final class OrderReqByArrival implements Comparator<AcceptedReq> {
	  public static final OrderReqByArrival INSTANCE = new OrderReqByArrival();

	  private OrderReqByArrival() {}

	  @Override
	  public int compare(AcceptedReq req1, AcceptedReq req2) {
	    return Integer.valueOf(req1.getReqArrival()).compareTo(req2.getReqArrival());
	  }

	  @Override
	  public boolean equals(Object other) {
	    return other == OrderReqByArrival.INSTANCE;
	  }

	  private Object readResolve() {
	    return INSTANCE;
	  }
	}