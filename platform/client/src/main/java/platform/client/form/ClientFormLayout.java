package platform.client.form;

import platform.client.ClientActionProxy;
import platform.client.ContainerFocusListener;
import platform.client.FormFocusTraversalPolicy;
import platform.client.logics.ClientComponent;
import platform.client.logics.ClientContainer;
import platform.client.logics.ClientGroupObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ClientFormLayout extends JPanel {

    // главный контейнер, который будет использоваться при отрисовке формы
    private ClientFormContainer mainContainer;

    private FormFocusTraversalPolicy policy;

    public JComponent getComponent() {
        return mainContainer;
    }

    // объект, которому делегируется ответственность за расположение объектов на форме
    private SimplexLayout layoutManager;

    // отображение объектов от сервера на контейнеры для рисования
    private Map<ClientContainer, ClientFormContainer> contviews = new HashMap<ClientContainer, ClientFormContainer>();

    protected abstract void gainedFocus();

    private FocusListener focusListener;

    public ClientFormLayout(ClientContainer topContainer) {

        createContainerViews(topContainer);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(mainContainer);

        // приходится делать StrongRef, иначе он тут же соберется сборщиком мусора так как ContainerFocusListener держит его как WeakReference
        focusListener = new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                gainedFocus();
            }
        };

        ContainerFocusListener.addListener(this, focusListener);

        setFocusCycleRoot(true);
        policy = new FormFocusTraversalPolicy();
        setFocusTraversalPolicy(policy);

        // вот таким вот маразматичным способом делается, чтобы при нажатии мышкой в ClientFormController фокус оставался на ней, а не уходил куда-то еще
        // теоретически можно найти способ как это сделать не так извращенно, но копаться в исходниках Swing'а очень долго
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
            }
        });
    }

    private void createContainerViews(ClientContainer container) {

        ClientFormContainer formContainer = new ClientFormContainer(container);

        if (container.container == null) {
            mainContainer = formContainer;
            layoutManager = new SimplexLayout(mainContainer, container);
        } else {
            contviews.get(container.container).add(formContainer, container);
        }

        formContainer.setLayout(layoutManager);
        contviews.put(container, formContainer);

        for (ClientComponent child : container.children) {
            if (child instanceof ClientContainer) {
                createContainerViews((ClientContainer)child);
            }
        }
    }

    private Map<KeyStroke, Map<ClientGroupObject, KeyListener>> bindings = new HashMap<KeyStroke, Map<ClientGroupObject, KeyListener>>();

    public void addKeyBinding(KeyStroke ks, ClientGroupObject groupObject, KeyListener run) {
        if (!bindings.containsKey(ks))
            bindings.put(ks, new HashMap<ClientGroupObject, KeyListener>());
        bindings.get(ks).put(groupObject, run);
    }

    // реализуем "обратную" обработку нажатий кнопок
    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {

        // делаем так, чтобы первым нажатия клавиш обрабатывал GroupObject, у которого стоит фокус
        // хотя конечно идиотизм это делать таким образом
        Component comp = e.getComponent(); //KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        while (comp != null && !(comp instanceof Window) && comp != this) {
            if (comp instanceof JComponent) {
                ClientGroupObject groupObject = (ClientGroupObject)((JComponent)comp).getClientProperty("groupObject");
                if (groupObject != null) {
                    Map<ClientGroupObject, KeyListener> keyBinding = bindings.get(ks);
                    if (keyBinding != null && keyBinding.containsKey(groupObject)) {
                        keyBinding.get(groupObject).keyPressed(e);
                        return true;
                    }
                    break;
                }
            }
            comp = comp.getParent();
        }

        Map<ClientGroupObject, KeyListener> keyBinding = bindings.get(ks);
        if (keyBinding != null && !keyBinding.isEmpty())
            keyBinding.values().iterator().next().keyPressed(e);

        return super.processKeyBinding(ks, e, condition, pressed);
    }

    // добавляем визуальный компонент
    public boolean add(ClientComponent key, Component view) {
        if (!contviews.get(key.container).isAncestorOf(view)) {
            contviews.get(key.container).addComponent(view, key);
            contviews.get(key.container).repaint();
            if (key.defaultComponent){
                policy.addDefault(view);
            }
            return true;
        } else
            return false;
    }

    // удаляем визуальный компонент
    public boolean remove(ClientComponent key, Component view) {
       if (contviews.get(key.container).isAncestorOf(view)) {
            contviews.get(key.container).removeComponent(view);
            contviews.get(key.container).repaint();
            if (key.defaultComponent){
                policy.removeDefault(view);
            }
            return true;
       } else
            return false;
    }

    public void dropCaches() {
        layoutManager.dropCaches();
        mainContainer.revalidate();
    }

    public void addBinding(KeyStroke key, String id, AbstractAction action) {
        getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(key, id);
        getActionMap().put(id, new ClientActionProxy(action));
    }
}
