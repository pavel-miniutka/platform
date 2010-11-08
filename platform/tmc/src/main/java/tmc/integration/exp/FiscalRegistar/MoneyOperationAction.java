package tmc.integration.exp.FiscalRegistar;

import com.jacob.com.Dispatch;
import platform.interop.action.AbstractClientAction;
import platform.interop.action.ClientAction;
import platform.interop.action.ClientActionDispatcher;

import java.io.IOException;

public class MoneyOperationAction extends AbstractClientAction {
    public final static int CASH_IN = 5;
    public final static int CASH_OUT = 6;
    private final static int FONT = 2;
    int type;
    double count;

    public MoneyOperationAction(int type, double count) {
        this.type = type;
        this.count = count;
    }

    @Override
    public void dispatch(ClientActionDispatcher dispatcher) throws IOException {
        RuntimeException exception = null;
        Dispatch cashDispatch = FiscalReg.createDispatch(1);
        Dispatch.call(cashDispatch, "OpenFiscalDoc", type);

        try {

            int k = FiscalReg.printHeaderAndNumbers(cashDispatch);

            Dispatch.invoke(cashDispatch, "AddItem", Dispatch.Method, new Object[]{0, count, false,
                    0, 1, 0, 1, 1, 0, "шт.", FONT, 0, k++, 0}, new int[1]);

            Dispatch.call(cashDispatch, "AddTotal", FONT, 0, k++, 15);

            Dispatch.call(cashDispatch, "CloseFiscalDoc");
        } catch (RuntimeException e) {
            Dispatch.call(cashDispatch, "CancelFiscalDoc", false);
            exception = e;
        } finally {
            Dispatch.call(cashDispatch, "Close", true);
        }

        if (exception != null) {
            throw exception;
        }
    }
}
