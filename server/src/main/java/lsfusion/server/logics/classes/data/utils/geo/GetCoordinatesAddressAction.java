package lsfusion.server.logics.classes.data.utils.geo;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.common.base.Throwables;
import lsfusion.server.data.sql.exception.SQLHandledException;
import lsfusion.server.data.value.DataObject;
import lsfusion.server.language.ScriptingErrorLog;
import lsfusion.server.language.ScriptingLogicsModule;
import lsfusion.server.logics.action.controller.context.ExecutionContext;
import lsfusion.server.logics.action.session.DataSession;
import lsfusion.server.logics.classes.ValueClass;
import lsfusion.server.logics.property.classes.ClassPropertyInterface;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Iterator;

public class GetCoordinatesAddressAction extends GeoAction {
    private final ClassPropertyInterface POIInterface;
    private final ClassPropertyInterface mapProviderInterface;

    public GetCoordinatesAddressAction(ScriptingLogicsModule LM, ValueClass... classes) {
        super(LM, classes);

        Iterator<ClassPropertyInterface> i = interfaces.iterator();
        POIInterface = i.next();
        mapProviderInterface = i.next();
    }

    public void executeInternal(ExecutionContext<ClassPropertyInterface> context) throws SQLHandledException {
        try {
            DataSession session = context.getSession();
            DataObject fullAddress = context.getDataKeyValue(POIInterface);
            DataObject mapProvider = context.getDataKeyValue(mapProviderInterface);

            BigDecimal longitude = null;
            BigDecimal latitude = null;
            String address = (String) fullAddress.object;
            if (address != null) {

                if (isYandex(context, mapProvider)) {

                    String url = "https://geocode-maps.yandex.ru/1.x/?geocode=" + address.trim().replace(" ", "+") + "&results=1&format=json";

                    final JSONObject response = JsonReader.read(url);
                    JSONObject objectCollection = response.getJSONObject("response").getJSONObject("GeoObjectCollection");
                    JSONObject featureMember = (JSONObject) objectCollection.getJSONArray("featureMember").get(0);
                    JSONObject point = featureMember.getJSONObject("GeoObject").getJSONObject("Point");
                    String[] position = point.getString("pos").split(" ");

                    longitude = new BigDecimal(position[0]);
                    latitude = new BigDecimal(position[1]);
                } else {

                    final Geocoder geocoder = new Geocoder();
                    GeocoderRequest geocoderRequest = new GeocoderRequestBuilder().setAddress(address).setLanguage("ru").getGeocoderRequest();
                    GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);

                    if (geocoderResponse != null && geocoderResponse.getResults().size() != 0) {
                        GeocoderResult result = geocoderResponse.getResults().get(0);

                        longitude = result.getGeometry().getLocation().getLng();
                        latitude = result.getGeometry().getLocation().getLat();
                    }
                }

                findProperty("readLatitude[]").change(latitude, session);
                findProperty("readLongitude[]").change(longitude, session);
            }
        } catch (IOException | JSONException | SQLException | ScriptingErrorLog.SemanticErrorException e) {
            throw Throwables.propagate(e);
        }
    }
}
