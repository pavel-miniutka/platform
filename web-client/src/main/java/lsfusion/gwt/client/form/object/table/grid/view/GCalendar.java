package lsfusion.gwt.client.form.object.table.grid.view;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.i18n.client.LocaleInfo;
import lsfusion.gwt.client.base.jsni.NativeHashMap;
import lsfusion.gwt.client.form.controller.GFormController;
import lsfusion.gwt.client.form.object.GGroupObjectValue;
import lsfusion.gwt.client.form.object.table.grid.controller.GGridController;

import java.util.ArrayList;
import java.util.List;

public class GCalendar extends GTippySimpleStateTableView {

    private final String calendarDateType;
    private JavaScriptObject calendar;

    public GCalendar(GFormController form, GGridController grid, String calendarDateType) {
        super(form, grid);
        this.calendarDateType = calendarDateType;
    }

    @Override
    public int getDefaultPageSize() {
        return 10;
    }

    @Override
    protected void render(Element element, JsArray<JavaScriptObject> list) {
        if (calendar == null) {
            //fullcalendar bug - https://github.com/fullcalendar/fullcalendar/issues/5863
            //to prevent this when calendar-element height less then ~350px
            element.getParentElement().getStyle().setProperty("overflow", "auto");
            element.getStyle().setProperty("minHeight", "400px");
            element.getStyle().setProperty("cursor", "default");
            String locale = LocaleInfo.getCurrentLocale().getLocaleName();

            calendar = createCalendar(element, controller, calendarDateType, locale);
        }
        updateEvents(list);
    }

    @Override
    public void onResize() {
        if (calendar != null)
            resize(calendar);
    }

    protected native void resize(JavaScriptObject calendar)/*-{
        calendar.updateSize();
    }-*/;

    protected native JavaScriptObject createCalendar(Element element, JavaScriptObject controller, String calendarDateType, String locale)/*-{
        var calendar = new $wnd.FullCalendar.Calendar(element, {
            initialView: 'dayGridMonth',
            height: 'parent',
            timeZone: 'UTC',
            locale: locale,
            firstDay: 1,
            initialDate: controller.getCurrentDay(calendarDateType),
            headerToolbar: {
                left: 'prev,next today',
                center: 'title',
                right: calendarDateType.includes('dateTime') ? 'dayGridMonth,dayGridWeek,timeGridDay' : 'dayGridMonth,dayGridWeek'
            },
            dayMaxEvents: true,
            //to prevent the expand of a single event without "end"-param to the next day "nextDayThreshold" should be equal to "defaultTimedEventDuration", which by default is 01:00:00
            nextDayThreshold: '01:00:00',
            eventOrder: 'start,index',
            eventChange: function (info) {
                changeProperty(info, 'start', this.objects);
                changeProperty(info, 'end', this.objects);
            },
            datesSet: function () {
                var filterLeftBorder = parseCalendarDateElement(calendar.view.activeStart);
                var filterRightBorder = parseCalendarDateElement(calendar.view.activeEnd);
                controller.setViewFilter(filterLeftBorder.year, filterLeftBorder.month, filterLeftBorder.day, filterRightBorder.year,
                    filterRightBorder.month, filterRightBorder.day, calendarDateType, calendarDateType.toLowerCase().includes('time'), 1000);
            },
            eventClick: function (info) {
                changeCurrentEvent(info.event, info.el);
            }
        });
        calendar.render();
        return calendar;

        function changeCurrentEvent(newEvent, elementClicked) {
            var newEventId= newEvent.id;
            var newObject = calendar.objects.get(newEventId);

            controller.changeSimpleGroupObject(newObject, true, elementClicked); // we're rerendering current event below

            @GCalendar::highlightEvent(*)(calendar, newEventId);
        }

        function changeProperty(info, position, objects) {
            var currentEvent = info.event;
            var oldEvent = info.oldEvent;
            if (currentEvent[position] !== null && oldEvent[position] !== null && currentEvent[position].getTime() !== oldEvent[position].getTime()) {
                var propertyName = currentEvent.extendedProps[position + 'FieldName'];
                var controllerFunction = propertyName.includes('dateTime') ? 'changeDateTimeProperty' : 'changeDateProperty';
                var eventElement = parseCalendarDateElement(currentEvent[position]);
                controller[controllerFunction](propertyName, objects.get(currentEvent.id), eventElement.year,
                    eventElement.month, eventElement.day, eventElement.hour, eventElement.minute, eventElement.second);
            }
        }

        function parseCalendarDateElement(element) {
            return {
                year: element.getFullYear(),
                month: element.getMonth() + 1,
                day: element.getUTCDate(),
                hour: element.getUTCHours(),
                minute: element.getUTCMinutes(),
                second: element.getUTCSeconds()
            }
        }
    }-*/;

    private final NativeHashMap<GGroupObjectValue, Event> events = new NativeHashMap<>();
    private class Event {

        public String title;
        public String start;
        public String end;
        public boolean editable;
        public boolean durationEditable;
        public final boolean allDay;
        public final GGroupObjectValue id;
        public final String startFieldName;
        public final String endFieldName;
        public final int index;

        public Event(JavaScriptObject object, int index) {
            String endEventFieldName = calendarDateType.contains("From") ? calendarDateType.replace("From", "To") : null;

            title = getTitle(object, getCaptions(new NativeHashMap<>(), gPropertyDraw -> gPropertyDraw.baseType.isId()));
            start = getStart(object, calendarDateType);
            end = endEventFieldName != null ? getEnd(object, endEventFieldName): null;
            editable = isEditable(object, controller, calendarDateType, endEventFieldName);
            durationEditable = isDurationEditable(object, controller, endEventFieldName);
            allDay = calendarDateType.equals("date") || calendarDateType.equals("dateFrom");
            id = getKey(object);
            startFieldName = calendarDateType;
            endFieldName = endEventFieldName;
            this.index = index;
        }
    }

    private void updateEvents(JsArray<JavaScriptObject> list) {
        NativeHashMap<GGroupObjectValue, Event> oldEvents = new NativeHashMap<>();
        oldEvents.putAll(events);
        List<Event> eventsToAdd = new ArrayList<>();
        int updateCol = 0;
        boolean needFullUpdate = false;
        for (int i = 0; i < list.length(); i++) {
            if (updateCol > 10) {
                needFullUpdate = true;
                break;
            }

            JavaScriptObject object = list.get(i);
            GGroupObjectValue key = getKey(object);

            Event event = new Event(object, i);
            events.put(key, event);

            Event oldEvent = oldEvents.remove(key);
            if (oldEvent == null) {
                eventsToAdd.add(event);
                updateCol++;
            } else {
                JavaScriptObject calendarEvent = getCalendarEventById(calendar, oldEvent.id);

                if (oldEvent.title == null || !oldEvent.title.equals(event.title)) {
                    updateCalendarProperty("title", event.title, calendarEvent);
                    updateCol++;
                }

                if (!oldEvent.start.equals(event.start)) {
                    updateStart(event.start, calendarEvent);
                    updateCol++;
                }

                if (oldEvent.end != null && !oldEvent.end.equals(event.end)) {
                    updateEnd(event.end, calendarEvent);
                    updateCol++;
                }

                if (oldEvent.editable != event.editable) {
                    updateCalendarProperty("editable", event.editable, calendarEvent);
                    updateCol++;
                }

                if (oldEvent.durationEditable != event.durationEditable) {
                    updateCalendarProperty("durationEditable", event.durationEditable, calendarEvent);
                    updateCol++;
                }
            }
        }

        if (!oldEvents.isEmpty() && oldEvents.size() < 10)
            oldEvents.foreachEntry((key, event) -> {
                removeSingleCalendarEvent(calendar, key);
                events.remove(key);
            });

        if (needFullUpdate) {
            events.clear();
            for (int i = 0; i < list.length(); i++) {
                JavaScriptObject object = list.get(i);
                events.put(getKey(object), new Event(object, i));
            }
            setCalendarEvents(calendar, createCalendarEventsObject(events));
        } else if (!eventsToAdd.isEmpty()) {
            eventsToAdd.forEach(eventToAdd -> addSingleCalendarEvent(calendar, getJsEvent(eventToAdd)));
        }

        setExtendedProp(calendar, "objects", list);
        highlightEvent(calendar, getCurrentKey());
    }

    private JsArray<JavaScriptObject> createCalendarEventsObject(NativeHashMap<GGroupObjectValue, Event> events){
        JsArray<JavaScriptObject> calendarEvents = JavaScriptObject.createArray().cast();
        events.foreachValue(event -> calendarEvents.push(getJsEvent(event)));
        return calendarEvents;
    }

    protected native void removeSingleCalendarEvent(JavaScriptObject calendar, GGroupObjectValue eventId)/*-{
        var event = calendar.getEventById(eventId);
        if (event != null)
            event.remove();
    }-*/;

    protected native void addSingleCalendarEvent(JavaScriptObject calendar, JavaScriptObject event)/*-{
        calendar.addEvent(event);
    }-*/;

    protected static native void highlightEvent(JavaScriptObject calendar, GGroupObjectValue id)/*-{
        var oldEvent = calendar.currentEventId != null ? calendar.getEventById(calendar.currentEventId) : null;
        if (oldEvent != null)
            oldEvent.setProp('classNames', '');
        var newEvent = calendar.getEventById(id);
        if (newEvent != null)
            newEvent.setProp('classNames', 'event-highlight');
        calendar.currentEventId = id;
    }-*/;

    protected native JavaScriptObject getCalendarEventById(JavaScriptObject calendar, GGroupObjectValue id)/*-{
        return calendar.getEventById(id);
    }-*/;

    protected native void setCalendarEvents(JavaScriptObject calendar, JsArray<JavaScriptObject> events)/*-{
        calendar.setOption('events', events);
    }-*/;

    protected native void updateCalendarProperty(String propertyName, Object property, JavaScriptObject event)/*-{
        event.setProp(propertyName, property);
    }-*/;

    protected native void updateStart(String start, JavaScriptObject event)/*-{
        event.setStart(start);
    }-*/;

    protected native void updateEnd(String end, JavaScriptObject event)/*-{
        event.setEnd(end);
    }-*/;

    protected native void setExtendedProp(JavaScriptObject calendar, String propertyName, JsArray<JavaScriptObject> list)/*-{
        var map = new Map();
        for (var i = 0; i < list.length; i++) {
            map.set(this.@GSimpleStateTableView::getKey(*)(list[i]).toString(), list[i]);
        }
        calendar.objects = map;
    }-*/;

    private JavaScriptObject getJsEvent(Event event){
        return getEventAsJs(event.title, event.start, event.end, event.editable, event.durationEditable,
                event.allDay, event.id, event.startFieldName, event.endFieldName, event.index);
    }

    protected native JavaScriptObject getEventAsJs(String title, String start, String end, boolean editable, boolean durationEditable,
                                                 boolean allDay, GGroupObjectValue id, String startFieldName, String endFieldName, int index)/*-{
        return {
            title: title,
            start: start,
            end: end,
            editable: editable,
            durationEditable: durationEditable,
            allDay: allDay,
            id: id,
            startFieldName: startFieldName,
            endFieldName: endFieldName,
            index: index
        };
    }-*/;

    protected native String getTitle(JavaScriptObject object, JsArray<JavaScriptObject> columns)/*-{
        var title = '';
        for (var i = 0; i < columns.length; i++) {
            if (title !== '')
                continue;
            title = columns[i] === 'name' ? object[columns[i]] : '';
        }
        if (title === '' && columns.length >= 2) {
            for (var k = 0; k <= 2; k++) {
                var value = object[columns[k]];
                if (value != null)
                    title = title !== '' ? title + ' - ' + value : value;
            }
        }
        return title;
    }-*/;

    protected native String getStart(JavaScriptObject object, String startEventFieldName)/*-{
        return object[startEventFieldName];
    }-*/;

    protected native String getEnd(JavaScriptObject object, String endEventFieldName)/*-{
        return object[endEventFieldName];
    }-*/;

    protected native boolean isEditable(JavaScriptObject object, JavaScriptObject controller, String startEventFieldName, String endEventFieldName)/*-{
        return !controller.isPropertyReadOnly(startEventFieldName, object) && (endEventFieldName == null || !controller.isPropertyReadOnly(endEventFieldName, object));
    }-*/;

    protected native boolean isDurationEditable(JavaScriptObject object, JavaScriptObject controller, String endEventFieldName)/*-{
        return endEventFieldName !== null && !controller.isPropertyReadOnly(endEventFieldName, object);
    }-*/;
}
