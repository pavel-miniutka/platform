package lsfusion.server.data.expr.formula;

import lsfusion.base.col.interfaces.immutable.ImSet;
import lsfusion.base.mutability.TwinImmutableObject;
import lsfusion.server.base.caches.ParamLazy;
import lsfusion.server.data.caches.hash.HashContext;
import lsfusion.server.data.expr.Expr;
import lsfusion.server.data.expr.UnionExpr;
import lsfusion.server.data.expr.key.KeyType;
import lsfusion.server.data.query.compile.CompileSource;
import lsfusion.server.data.translate.ExprTranslator;
import lsfusion.server.data.translate.MapTranslate;
import lsfusion.server.data.where.Where;
import lsfusion.server.logics.classes.data.integral.IntegralClass;

// среднее что-то между CaseExpr и FormulaExpr - для того чтобы не плодить экспоненциальные case'ы
// придется делать BaseExpr
public class LinearExpr extends UnionExpr {

    final LinearOperandMap map;

    public LinearExpr(LinearOperandMap map) {
        this.map = map;
        assert (map.size()>0);
    }

    public String getSource(CompileSource compile, boolean needValue) {
        if(compile instanceof ToString)
            return map.toString();
        else
            return map.getSource(compile);
    }

    @Override
    protected ImSet<Expr> getParams() {
        return map.keys();
    }

    /*    @Override
    public boolean equals(Object obj) {
        if(map.size()==1) {
            Map.Entry<Expr, Integer> singleEntry = BaseUtils.singleEntry(map);
            if(singleEntry.getValue().equals(1)) return singleEntry.getKey().equals(obj);
        }
        return super.equals(obj);
    }*/

    public boolean calcTwins(TwinImmutableObject obj) {
        return map.equals(((LinearExpr)obj).map);
    }

    public int hash(HashContext hashContext) {
        return map.hashOuter(hashContext) * 5;
    }

    @ParamLazy
    public Expr translate(ExprTranslator translator) {
        Expr result = null;
        for(int i=0,size=map.size();i<size;i++) {
            Expr transOperand = map.getKey(i).translateExpr(translator).scale(map.getValue(i));
            if(result==null)
                result = transOperand;
            else
                result = result.sum(transOperand);
        }
        return result;
    }

    // транслирует выражение/ также дополнительно вытаскивает ExprCase'ы
    protected LinearExpr translate(MapTranslate translator) {
        return new LinearExpr(map.translateOuter(translator));
    }

    @Override
    public Expr packFollowFalse(Where where) {
        return map.packFollowFalse(where);
    }

    public IntegralClass getStaticClass(KeyType keyType) {
        return map.getType(keyType);
    }
}
