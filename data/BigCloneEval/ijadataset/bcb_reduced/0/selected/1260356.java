package com.tysanclan.site.projectewok.components;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tysanclan.site.projectewok.TysanPage;
import com.tysanclan.site.projectewok.TysanSession;
import com.tysanclan.site.projectewok.auth.TysanSecurity;
import com.tysanclan.site.projectewok.components.IconLink.DefaultClickResponder;
import com.tysanclan.site.projectewok.components.RequiresAttentionLink.IRequiresAttentionCondition;
import com.tysanclan.site.projectewok.entities.User;
import com.tysanclan.site.projectewok.entities.dao.AttentionSuppressionDAO;

/**
 * @author Jeroen Steenbeeke
 */
public abstract class TysanOverviewPanel<T> extends Panel {

    private static final long serialVersionUID = 1L;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = ElementType.CONSTRUCTOR)
    public static @interface NoAuto {
    }

    private static final Logger logger = LoggerFactory.getLogger(TysanOverviewPanel.class);

    @SpringBean
    private AttentionSuppressionDAO attentionSuppressionDAO;

    protected TysanOverviewPanel(String id, String title) {
        this(id, null, title);
    }

    private IModel<T> model;

    private List<String> notifications;

    protected TysanOverviewPanel(String id, IModel<T> model, String title) {
        super(id);
        this.model = model;
        this.notifications = new LinkedList<String>();
        add(new Label("title", title));
        add(new ListView<String>("states") {

            private static final long serialVersionUID = 1L;

            /**
			 * @see org.apache.wicket.markup.html.list.ListView#populateItem(org.apache.wicket.markup.html.list.ListItem)
			 */
            @Override
            protected void populateItem(ListItem<String> item) {
                item.add(new ContextImage("icanhaznotification", "images/icons/error.png"));
            }
        });
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        get("states").setDefaultModel(new ListModel<String>(notifications));
    }

    /**
	 * @see org.apache.wicket.Component#onDetach()
	 */
    @Override
    protected void onDetach() {
        super.onDetach();
        if (model != null) {
            model.detach();
        }
    }

    /**
	 * @return the model
	 */
    public IModel<T> getModel() {
        return model;
    }

    public T getModelObject() {
        return model.getObject();
    }

    protected RequiresAttentionLink createConditionalVisibilityLink(String id, final Class<? extends TysanPage> pageClass, String text, IRequiresAttentionCondition condition) {
        return createConditionalVisibilityLink(id, null, pageClass, text, condition);
    }

    protected <U> RequiresAttentionLink createConditionalVisibilityLink(String id, IModel<U> linkModel, final Class<? extends TysanPage> pageClass, String text, IRequiresAttentionCondition condition) {
        RequiresAttentionLink.Builder builder = createBasicBuilder(pageClass, linkModel, text);
        builder.setInvisibleIfNotNotified(true);
        builder.setConditionList(notifications);
        return builder.newInstance(id, condition, attentionSuppressionDAO);
    }

    private <U> RequiresAttentionLink.Builder createBasicBuilder(final Class<? extends TysanPage> pageClass, IModel<U> linkModel, String text) {
        RequiresAttentionLink.Builder builder = new RequiresAttentionLink.Builder(text, new DefaultClickResponder<U>(linkModel) {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("unchecked")
            @Override
            public void onClick() {
                Constructor<? extends TysanPage> modelParamConstructor = null;
                Constructor<? extends TysanPage> userParamConstructor = null;
                Constructor<? extends TysanPage> noParamConstructor = null;
                U modelObject = super.getModelObject();
                for (Constructor<?> cns : pageClass.getConstructors()) {
                    if (cns.getParameterTypes().length == 0) {
                        if (!cns.isAnnotationPresent(NoAuto.class)) {
                            noParamConstructor = (Constructor<? extends TysanPage>) cns;
                        }
                    }
                    if (cns.getParameterTypes().length == 1 && cns.getParameterTypes()[0] == User.class) {
                        if (!cns.isAnnotationPresent(NoAuto.class)) {
                            userParamConstructor = (Constructor<? extends TysanPage>) cns;
                        }
                    }
                    if (cns.getParameterTypes().length == 1 && modelObject != null && cns.getParameterTypes()[0].isAssignableFrom(modelObject.getClass())) {
                        if (!cns.isAnnotationPresent(NoAuto.class)) {
                            modelParamConstructor = (Constructor<? extends TysanPage>) cns;
                        }
                    }
                }
                try {
                    if (userParamConstructor != null) {
                        setResponsePage(userParamConstructor.newInstance(getUser()));
                    } else if (noParamConstructor != null) {
                        setResponsePage(noParamConstructor.newInstance());
                    } else if (modelParamConstructor != null) {
                        setResponsePage(modelParamConstructor.newInstance(modelObject));
                    }
                } catch (IllegalArgumentException e) {
                    logger.error(e.getMessage(), e);
                } catch (InstantiationException e) {
                    logger.error(e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    logger.error(e.getMessage(), e);
                } catch (InvocationTargetException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
        TysanSecurity sec = new TysanSecurity();
        if (!sec.authorize(pageClass)) {
            builder.disableVisibility();
        }
        return builder;
    }

    protected RequiresAttentionLink createLink(String id, final Class<? extends TysanPage> pageClass, String text, IRequiresAttentionCondition condition) {
        return createLink(id, null, pageClass, text, condition);
    }

    protected <U> RequiresAttentionLink createLink(String id, IModel<U> linkModel, final Class<? extends TysanPage> pageClass, String text, IRequiresAttentionCondition condition) {
        RequiresAttentionLink.Builder builder = createBasicBuilder(pageClass, linkModel, text);
        builder.setConditionList(notifications);
        return builder.newInstance(id, condition, attentionSuppressionDAO);
    }

    protected final User getUser() {
        TysanSession session = TysanSession.get();
        return session.getUser();
    }

    public void requiresAttention() {
        notifications.add("EXPLICIT");
    }
}
