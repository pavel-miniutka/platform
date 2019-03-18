package lsfusion.server.data.caches.hash;

import lsfusion.base.col.interfaces.immutable.ImSet;
import lsfusion.server.data.expr.key.ParamExpr;
import lsfusion.server.data.translate.MapTranslate;

public class HashCodeKeys implements HashKeys {

    private HashCodeKeys() {
    }
    public static final HashCodeKeys instance = new HashCodeKeys();

    public int hash(ParamExpr expr) {
        return expr.hashCode();
    }

    public HashKeys filterKeys(ImSet<ParamExpr> keys) {
        return this;
    }

    public HashKeys reverseTranslate(MapTranslate translator, ImSet<ParamExpr> keys) {
        if(translator.identityKeys(keys))
            return this;
        else
            return null;
    }

    public boolean isGlobal() {
        return true;
    }
}
