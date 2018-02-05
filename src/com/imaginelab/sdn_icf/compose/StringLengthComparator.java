package com.imaginelab.sdn_icf.compose;
import java.util.Comparator;

import com.imaginelab.sdn_icf.containers.IaaSRequest;

public class StringLengthComparator implements Comparator<IaaSRequest>
{
    @Override
    public int compare(IaaSRequest x, IaaSRequest y)
    {
        // Assume neither string is null. Real code should
    	// compare the arrival times for the requests
        // probably be more robust
        if (x.getArrivalTime() < y.getArrivalTime())
        {
            return -1;
        }
        if (x.getArrivalTime() > y.getArrivalTime())
        {
            return 1;
        }
        return 0;
    }
}
