package lsfusion.gwt.client.form.object.table.grid.view;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import lsfusion.gwt.client.base.jsni.NativeHashMap;
import lsfusion.gwt.client.form.controller.GFormController;
import lsfusion.gwt.client.form.object.table.grid.controller.GGridController;

public class GCalendar extends GTippySimpleStateTableView {

    private final String calendarDateType;
    private JavaScriptObject calendar;
    private int pageSize = 10;

    public GCalendar(GFormController form, GGridController grid, String calendarDateType) {
        super(form, grid);
        this.calendarDateType = calendarDateType;
    }

    @Override
    protected void render(Element element, JsArray<JavaScriptObject> list) {
        if (calendar == null) {
            //fullcalendar bug - https://github.com/fullcalendar/fullcalendar/issues/5863
            //to prevent this when calendar-element height less then ~350px
            element.getParentElement().getStyle().setProperty("overflow", "auto");
            element.getStyle().setProperty("minHeight", "400px");
            element.getStyle().setProperty("cursor", "default");

            calendar = createCalendar(element, controller, calendarDateType);
        }
        updateEvents(calendar, list, getCaptions(new NativeHashMap<>(), gPropertyDraw -> gPropertyDraw.baseType.isId()), controller);
    }

    @Override
    public void onResize() {
        if (calendar != null)
            resize(calendar);
    }

    @Override
    public int getPageSize() {
        return this.pageSize;
    }

    protected void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    protected native void resize(JavaScriptObject calendar)/*-{
        calendar.updateSize();
    }-*/;

    protected boolean isCurrentObjectKey(JavaScriptObject object){
        return isCurrentKey(getKey(object));
    }

    protected native JavaScriptObject createCalendar(Element element, JavaScriptObject controller, String calendarDateType)/*-{
        var calendar = new $wnd.FullCalendar.Calendar(element, {
            initialView: 'dayGridMonth',
            height: 'parent',
            timeZone: 'UTC',
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
                    filterRightBorder.month, filterRightBorder.day, calendarDateType, calendarDateType.toLowerCase().includes('time'));
            },
            eventClick: function (info) {
                changeCurrentEvent(info.event, info.el);
            }
        });
        calendar.render();
        return calendar;

        function changeCurrentEvent(newEvent, elementClicked) {
            var newEventIndex = newEvent.extendedProps.index;
            var newObject = calendar.objects[newEventIndex];

            controller.changeSimpleGroupObject(newObject, true, elementClicked); // we're rerendering current event below

            var oldEvent = calendar.currentEventIndex != null ? calendar.getEvents()[calendar.currentEventIndex] : null;
            if (oldEvent !== null)
                oldEvent.setProp('classNames', '');
            newEvent.setProp('classNames', 'event-highlight');
            calendar.currentEventIndex = newEventIndex;
        }

        function changeProperty(info, position, objects) {
            var currentEvent = info.event;
            var oldEvent = info.oldEvent;
            if (currentEvent[position] !== null && currentEvent[position].getTime() !== oldEvent[position].getTime()) {
                var propertyName = currentEvent.extendedProps[position + 'FieldName'];
                var controllerFunction = propertyName.includes('dateTime') ? 'changeDateTimeProperty' : 'changeDateProperty';
                var eventElement = parseCalendarDateElement(currentEvent[position]);
                controller[controllerFunction](propertyName, objects[currentEvent.extendedProps.index], eventElement.year,
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

    protected native JavaScriptObject updateEvents(JavaScriptObject calendar, JavaScriptObject objects, JsArray<JavaScriptObject> columns, JavaScriptObject controller)/*-{
        var events = [];
        var calendarDateType = this.@GCalendar::calendarDateType;
        calendar.currentEventIndex = null;
        var startEventFieldName = calendarDateType;
        var endEventFieldName = startEventFieldName.includes('From') ? startEventFieldName.replace('From', 'To') : null;
        for (var i = 0; i < objects.length; i++) {
            var object = objects[i]
            var isCurrentKey = this.@GCalendar::isCurrentObjectKey(*)(object);
            var event = {
                'title': getTitle(object),
                'start': object[startEventFieldName],
                'end': endEventFieldName != null ? object[endEventFieldName] : null,
                'editable': !controller.isPropertyReadOnly(startEventFieldName, object) && (endEventFieldName == null || !controller.isPropertyReadOnly(endEventFieldName, object)),
                'durationEditable': endEventFieldName !== null && !controller.isPropertyReadOnly(endEventFieldName, object),
                'allDay': calendarDateType === 'date' || calendarDateType === 'dateFrom',
                'index': i,
                'startFieldName': startEventFieldName,
                'endFieldName': endEventFieldName,
                'classNames': isCurrentKey ? 'event-highlight' : ''
            };
            events.push(event);
            if (isCurrentKey)
                calendar.currentEventIndex = i;
        }

        calendar.objects = objects;
        calendar.setOption('events', events);

        function getTitle(object) {
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
        }
    }-*/;
}
