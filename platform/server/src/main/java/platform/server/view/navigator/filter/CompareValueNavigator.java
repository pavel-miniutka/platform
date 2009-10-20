package platform.server.view.navigator.filter;

import platform.server.view.form.filter.CompareValue;
import platform.server.view.navigator.ObjectNavigator;
import platform.server.view.navigator.Mapper;

import java.sql.SQLException;
import java.util.Set;

public interface CompareValueNavigator {

    CompareValue doMapping(Mapper mapper);

    void fillObjects(Set<ObjectNavigator> objects);
}
