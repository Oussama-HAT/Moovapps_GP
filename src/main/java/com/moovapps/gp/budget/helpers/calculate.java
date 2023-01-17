package com.moovapps.gp.budget.helpers;

import java.math.BigDecimal;

public class calculate {

    public static double RB_calculateCurrentRAP(double totalEngagement , double totalPaiement , double rapLibere , double totalDiminution){
        return (totalEngagement - totalPaiement - rapLibere - totalDiminution);
    }

    public static BigDecimal castToBigDecimal(Object value){
        BigDecimal bigDecimalValue = null;
        try {
            bigDecimalValue = (BigDecimal)value;
        } catch (ClassCastException e) {
            if (value != null && value instanceof Long)
                bigDecimalValue = new BigDecimal(((Long)value).longValue());
            if (value != null && value instanceof Float)
                bigDecimalValue = new BigDecimal(((Float)value).floatValue());
            if (value != null && value instanceof Double)
                bigDecimalValue = new BigDecimal(((Double)value).doubleValue());
        }
        return bigDecimalValue;
    }

}
