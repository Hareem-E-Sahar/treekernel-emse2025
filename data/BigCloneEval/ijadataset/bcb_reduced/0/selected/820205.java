package blue.automation;

import blue.automation.*;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.apache.commons.lang.builder.ToStringBuilder;
import blue.Arrangement;
import blue.BlueData;
import blue.InstrumentAssignment;
import blue.SoundLayer;
import blue.mixer.Channel;
import blue.mixer.ChannelList;
import blue.mixer.ChannelListListener;
import blue.mixer.Effect;
import blue.mixer.EffectsChain;
import blue.mixer.Mixer;
import blue.mixer.Send;
import blue.orchestra.Instrument;
import blue.soundObject.PolyObject;
import blue.soundObject.PolyObjectListener;
import blue.soundObject.editor.ceciliaModule.LineColors;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Controller to handle synchronizing of UI with time, directing single edits in
 * control UI to values in parameter lines.
 * 
 * Builds list of Automations for SoundLayerPanels. (Traverses Arrangement and
 * Mixer)
 * 
 * Removes Automations from SoundLayers when Parameter is removed.
 * 
 * @author steven
 */
public class AutomationManager implements ParameterListListener, AutomatableCollectionListener, ChannelListListener, PolyObjectListener {

    /**
     * cache of all parameters in project
     */
    private ArrayList<Parameter> allParameters = new ArrayList<Parameter>();

    BlueData data = null;

    PolyObject pObj = null;

    ActionListener parameterActionListener;

    private SoundLayer selectedSoundLayer = null;

    private static AutomationManager instance = null;

    PropertyChangeListener renderTimeListener;

    private AutomationManager() {
        parameterActionListener = new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (data == null || selectedSoundLayer == null) {
                    return;
                }
                JMenuItem menuItem = (JMenuItem) ae.getSource();
                Parameter param = (Parameter) menuItem.getClientProperty("param");
                parameterSelected(selectedSoundLayer, param);
                selectedSoundLayer = null;
            }
        };
        renderTimeListener = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent pce) {
                if (pce.getSource() == data) {
                    if (pce.getPropertyName().equals("renderStartTime")) {
                        updateValuesFromAutomations();
                    }
                }
            }
        };
    }

    public static AutomationManager getInstance() {
        if (instance == null) {
            instance = new AutomationManager();
        }
        return instance;
    }

    /**
     * When parameter is selected, check if this parameter is in use and if by
     * this soundLayer, turn it off, otherwise move it to this soundLayer.
     * 
     * @param soundLayer
     * @param param
     */
    private void parameterSelected(SoundLayer soundLayer, Parameter param) {
        ParameterIdList paramIdList = soundLayer.getAutomationParameters();
        String uniqueId = param.getUniqueId();
        if (param.isAutomationEnabled()) {
            if (paramIdList.contains(uniqueId)) {
                param.setAutomationEnabled(false);
                paramIdList.removeParameterId(uniqueId);
            } else {
                PolyObject pObj = data.getPolyObject();
                for (int i = 0; i < pObj.getSize(); i++) {
                    SoundLayer layer = (SoundLayer) pObj.getElementAt(i);
                    if (layer == soundLayer) {
                        continue;
                    }
                    ParameterIdList automationParameters = layer.getAutomationParameters();
                    if (automationParameters.contains(uniqueId)) {
                        automationParameters.removeParameterId(uniqueId);
                    }
                }
                paramIdList.addParameterId(uniqueId);
            }
        } else {
            param.setAutomationEnabled(true);
            param.getLine().setColor(LineColors.getColor(paramIdList.size()));
            paramIdList.addParameterId(uniqueId);
            param.fireUpdateFromTimeChange();
        }
    }

    public void setData(BlueData data) {
        if (this.data != null) {
            Arrangement arrangement = this.data.getArrangement();
            arrangement.removeAutomatableCollectionListener(this);
            for (int i = 0; i < arrangement.size(); i++) {
                Instrument instr = arrangement.getInstrument(i);
                if (instr instanceof Automatable) {
                    Automatable temp = (Automatable) instr;
                    ParameterList parameters = temp.getParameterList();
                    parameters.removeParameterListListener(this);
                }
            }
            Mixer mixer = this.data.getMixer();
            ChannelList channels = mixer.getChannels();
            channels.removeChannelListListener(this);
            for (int i = 0; i < channels.size(); i++) {
                removeListenerFromChannel(channels.getChannel(i));
            }
            ChannelList subChannels = mixer.getSubChannels();
            subChannels.removeChannelListListener(this);
            for (int i = 0; i < subChannels.size(); i++) {
                removeListenerFromChannel(subChannels.getChannel(i));
            }
            removeListenerFromChannel(mixer.getMaster());
            if (this.pObj != null) {
                this.pObj.removePolyObjectListener(this);
            }
            this.data.removePropertyChangeListener(renderTimeListener);
        }
        allParameters.clear();
        if (data == null) {
            return;
        }
        Arrangement arrangement = data.getArrangement();
        for (int i = 0; i < arrangement.size(); i++) {
            Instrument instr = arrangement.getInstrument(i);
            if (instr instanceof Automatable) {
                Automatable temp = (Automatable) instr;
                ParameterList parameters = temp.getParameterList();
                allParameters.addAll(parameters.getParameters());
                parameters.addParameterListListener(this);
            }
        }
        arrangement.addAutomatableCollectionListener(this);
        Mixer mixer = data.getMixer();
        ChannelList channels = mixer.getChannels();
        channels.addChannelListListener(this);
        for (int i = 0; i < channels.size(); i++) {
            addListenerToChannel(channels.getChannel(i));
        }
        ChannelList subChannels = mixer.getSubChannels();
        subChannels.addChannelListListener(this);
        for (int i = 0; i < subChannels.size(); i++) {
            addListenerToChannel(subChannels.getChannel(i));
        }
        addListenerToChannel(mixer.getMaster());
        this.data = data;
        this.pObj = data.getPolyObject();
        this.pObj.addPolyObjectListener(this);
        this.data.addPropertyChangeListener(renderTimeListener);
    }

    private void addListenerToChannel(Channel channel) {
        EffectsChain pre = channel.getPreEffects();
        pre.addAutomatableCollectionListener(this);
        for (int i = 0; i < pre.size(); i++) {
            ParameterList parameterList = ((Automatable) pre.getElementAt(i)).getParameterList();
            parameterList.addParameterListListener(this);
            allParameters.addAll(parameterList.getParameters());
        }
        EffectsChain post = channel.getPostEffects();
        post.addAutomatableCollectionListener(this);
        for (int i = 0; i < post.size(); i++) {
            ParameterList parameterList = ((Automatable) post.getElementAt(i)).getParameterList();
            parameterList.addParameterListListener(this);
            allParameters.addAll(parameterList.getParameters());
        }
        allParameters.add(channel.getLevelParameter());
    }

    private void removeListenerFromChannel(Channel channel) {
        EffectsChain pre = channel.getPreEffects();
        pre.removeAutomatableCollectionListener(this);
        for (int i = 0; i < pre.size(); i++) {
            ((Automatable) pre.getElementAt(i)).getParameterList().removeParameterListListener(this);
        }
        EffectsChain post = channel.getPostEffects();
        post.removeAutomatableCollectionListener(this);
        for (int i = 0; i < post.size(); i++) {
            ((Automatable) post.getElementAt(i)).getParameterList().removeParameterListListener(this);
        }
    }

    public JPopupMenu getAutomationMenu(SoundLayer soundLayer) {
        this.selectedSoundLayer = soundLayer;
        JPopupMenu menu = new JPopupMenu();
        JMenu instrRoot = new JMenu("Instrument");
        Arrangement arrangement = data.getArrangement();
        ParameterIdList paramIdList = soundLayer.getAutomationParameters();
        for (int i = 0; i < arrangement.size(); i++) {
            InstrumentAssignment ia = arrangement.getInstrumentAssignment(i);
            if (ia.enabled && ia.instr instanceof Automatable) {
                ParameterList params = ((Automatable) ia.instr).getParameterList();
                if (params.size() <= 0) {
                    continue;
                }
                JMenu instrMenu = new JMenu();
                instrMenu.setText(ia.arrangementId + ") " + ia.instr.getName());
                for (int j = 0; j < params.size(); j++) {
                    Parameter param = params.getParameter(j);
                    JMenuItem paramItem = new JMenuItem();
                    paramItem.setText(param.getName());
                    paramItem.addActionListener(parameterActionListener);
                    if (param.isAutomationEnabled()) {
                        if (paramIdList.contains(param.getUniqueId())) {
                            paramItem.setForeground(Color.GREEN);
                        } else {
                            paramItem.setForeground(Color.ORANGE);
                        }
                    }
                    paramItem.putClientProperty("instr", ia.instr);
                    paramItem.putClientProperty("param", param);
                    instrMenu.add(paramItem);
                }
                instrRoot.add(instrMenu);
            }
        }
        menu.add(instrRoot);
        Mixer mixer = data.getMixer();
        if (mixer.isEnabled()) {
            JMenu mixerRoot = new JMenu("Mixer");
            ChannelList channels = mixer.getChannels();
            if (channels.size() > 0) {
                JMenu channelsMenu = new JMenu("Channels");
                for (int i = 0; i < channels.size(); i++) {
                    channelsMenu.add(buildChannelMenu(channels.getChannel(i), soundLayer));
                }
                mixerRoot.add(channelsMenu);
            }
            ChannelList subChannels = mixer.getSubChannels();
            if (subChannels.size() > 0) {
                JMenu subChannelsMenu = new JMenu("Sub-Channels");
                for (int i = 0; i < subChannels.size(); i++) {
                    subChannelsMenu.add(buildChannelMenu(subChannels.getChannel(i), soundLayer));
                }
                mixerRoot.add(subChannelsMenu);
            }
            Channel master = mixer.getMaster();
            mixerRoot.add(buildChannelMenu(master, soundLayer));
            menu.add(mixerRoot);
        }
        menu.addSeparator();
        JMenuItem clearAll = new JMenuItem("Clear All");
        clearAll.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Object retVal = DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation("Please Confirm Clearing All Parameter Data for this SoundLayer"));
                if (retVal == NotifyDescriptor.YES_OPTION) {
                    ParameterIdList idList = selectedSoundLayer.getAutomationParameters();
                    Iterator iter = new ArrayList(idList.getParameters()).iterator();
                    while (iter.hasNext()) {
                        String paramId = (String) iter.next();
                        Parameter param = getParameter(paramId);
                        param.setAutomationEnabled(false);
                        idList.removeParameterId(paramId);
                    }
                }
            }
        });
        menu.add(clearAll);
        clearAll.setEnabled(soundLayer.getAutomationParameters().size() > 0);
        return menu;
    }

    private JMenu buildChannelMenu(Channel channel, SoundLayer soundLayer) {
        JMenu retVal = new JMenu();
        retVal.setText(channel.getName());
        ParameterIdList paramIdList = soundLayer.getAutomationParameters();
        EffectsChain preEffects = channel.getPreEffects();
        if (preEffects.size() > 0) {
            JMenu preMenu = new JMenu("Pre-Effects");
            retVal.add(preMenu);
            for (int i = 0; i < preEffects.size(); i++) {
                Automatable automatable = (Automatable) preEffects.getElementAt(i);
                ParameterList params = automatable.getParameterList();
                if (params.size() > 0) {
                    JMenu effectMenu = new JMenu();
                    if (automatable instanceof Effect) {
                        effectMenu.setText(((Effect) automatable).getName());
                    } else if (automatable instanceof Send) {
                        effectMenu.setText("Send: " + ((Send) automatable).getSendChannel());
                    } else {
                        effectMenu.setText("ERROR");
                    }
                    preMenu.add(effectMenu);
                    for (int j = 0; j < params.size(); j++) {
                        Parameter param = params.getParameter(j);
                        JMenuItem paramItem = new JMenuItem();
                        paramItem.setText(param.getName());
                        paramItem.addActionListener(parameterActionListener);
                        if (param.isAutomationEnabled()) {
                            if (paramIdList.contains(param.getUniqueId())) {
                                paramItem.setForeground(Color.GREEN);
                            } else {
                                paramItem.setForeground(Color.ORANGE);
                            }
                        }
                        paramItem.putClientProperty("param", param);
                        effectMenu.add(paramItem);
                    }
                }
            }
        }
        JMenuItem volItem = new JMenuItem("Volume");
        Parameter volParam = channel.getLevelParameter();
        volItem.putClientProperty("param", volParam);
        volItem.addActionListener(parameterActionListener);
        if (volParam.isAutomationEnabled()) {
            if (paramIdList.contains(volParam.getUniqueId())) {
                volItem.setForeground(Color.GREEN);
            } else {
                volItem.setForeground(Color.ORANGE);
            }
        }
        retVal.add(volItem);
        EffectsChain postEffects = channel.getPostEffects();
        if (postEffects.size() > 0) {
            JMenu postMenu = new JMenu("Post-Effects");
            retVal.add(postMenu);
            for (int i = 0; i < postEffects.size(); i++) {
                Automatable automatable = (Automatable) postEffects.getElementAt(i);
                ParameterList params = automatable.getParameterList();
                if (params.size() > 0) {
                    JMenu effectMenu = new JMenu();
                    if (automatable instanceof Effect) {
                        effectMenu.setText(((Effect) automatable).getName());
                    } else if (automatable instanceof Send) {
                        effectMenu.setText("Send: " + ((Send) automatable).getSendChannel());
                    } else {
                        effectMenu.setText("ERROR");
                    }
                    postMenu.add(effectMenu);
                    for (int j = 0; j < params.size(); j++) {
                        Parameter param = params.getParameter(j);
                        JMenuItem paramItem = new JMenuItem();
                        paramItem.setText(param.getName());
                        paramItem.addActionListener(parameterActionListener);
                        if (param.isAutomationEnabled()) {
                            if (paramIdList.contains(param.getUniqueId())) {
                                paramItem.setForeground(Color.GREEN);
                            } else {
                                paramItem.setForeground(Color.ORANGE);
                            }
                        }
                        paramItem.putClientProperty("param", param);
                        effectMenu.add(paramItem);
                    }
                }
            }
        }
        return retVal;
    }

    public void parameterAdded(Parameter param) {
        allParameters.add(param);
    }

    public void parameterRemoved(Parameter param) {
        allParameters.remove(param);
        PolyObject pObj = data.getPolyObject();
        for (int i = 0; i < pObj.getSize(); i++) {
            SoundLayer layer = (SoundLayer) pObj.getElementAt(i);
            ParameterIdList automationParameters = layer.getAutomationParameters();
            String paramId = param.getUniqueId();
            if (automationParameters.contains(paramId)) {
                automationParameters.removeParameterId(paramId);
            }
        }
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public void automatableAdded(Automatable automatable) {
        ParameterList params = automatable.getParameterList();
        params.addParameterListListener(this);
        allParameters.addAll(params.getParameters());
    }

    public void automatableRemoved(Automatable automatable) {
        ParameterList params = automatable.getParameterList();
        params.removeParameterListListener(this);
        ArrayList removedParamIds = new ArrayList();
        allParameters.removeAll(params.getParameters());
        for (int i = 0; i < params.size(); i++) {
            Parameter param = params.getParameter(i);
            removedParamIds.add(param.getUniqueId());
        }
        removeParameters(removedParamIds);
    }

    public Parameter getParameter(String paramId) {
        if (data == null) {
            return null;
        }
        for (Iterator iter = allParameters.iterator(); iter.hasNext(); ) {
            Parameter param = (Parameter) iter.next();
            if (param.getUniqueId().equals(paramId)) {
                return param;
            }
        }
        return null;
    }

    public void channelAdded(Channel channel) {
        addListenerToChannel(channel);
    }

    public void channelRemoved(Channel channel) {
        EffectsChain pre = channel.getPreEffects();
        pre.removeAutomatableCollectionListener(this);
        ArrayList<String> removedParamIds = new ArrayList<String>();
        for (int i = 0; i < pre.size(); i++) {
            ParameterList parameterList = ((Automatable) pre.getElementAt(i)).getParameterList();
            parameterList.removeParameterListListener(this);
            allParameters.removeAll(parameterList.getParameters());
            for (int j = 0; j < parameterList.size(); j++) {
                Parameter parameter = parameterList.getParameter(j);
                if (parameter.isAutomationEnabled()) {
                    removedParamIds.add(parameter.getUniqueId());
                }
            }
        }
        EffectsChain post = channel.getPostEffects();
        post.removeAutomatableCollectionListener(this);
        for (int i = 0; i < post.size(); i++) {
            ParameterList parameterList = ((Automatable) post.getElementAt(i)).getParameterList();
            parameterList.removeParameterListListener(this);
            allParameters.removeAll(parameterList.getParameters());
            for (int j = 0; j < parameterList.size(); j++) {
                Parameter parameter = parameterList.getParameter(j);
                if (parameter.isAutomationEnabled()) {
                    removedParamIds.add(parameter.getUniqueId());
                }
            }
        }
        Parameter levelParameter = channel.getLevelParameter();
        allParameters.remove(levelParameter);
        if (levelParameter.isAutomationEnabled()) {
            removedParamIds.add(levelParameter.getUniqueId());
        }
        removeParameters(removedParamIds);
    }

    private void removeParameters(ArrayList<String> paramIds) {
        if (paramIds == null || paramIds.size() == 0) {
            return;
        }
        PolyObject pObj = data.getPolyObject();
        for (int i = 0; i < pObj.getSize(); i++) {
            SoundLayer layer = (SoundLayer) pObj.getElementAt(i);
            ParameterIdList automationParameters = layer.getAutomationParameters();
            for (int j = automationParameters.size() - 1; j >= 0; j--) {
                String paramId = automationParameters.getParameterId(j);
                if (paramIds.contains(paramId)) {
                    automationParameters.removeParameterId(j);
                }
            }
        }
    }

    public void soundLayerAdded(SoundLayer sLayer) {
    }

    public void soundLayerRemoved(SoundLayer sLayer) {
        ParameterIdList idList = sLayer.getAutomationParameters();
        for (int i = 0; i < idList.size(); i++) {
            String paramId = idList.getParameterId(i);
            Parameter param = getParameter(paramId);
            if (param != null) {
                param.setAutomationEnabled(false);
            }
        }
    }

    protected void updateValuesFromAutomations() {
        Iterator iter = new ArrayList(allParameters).iterator();
        while (iter.hasNext()) {
            Parameter param = (Parameter) iter.next();
            if (param.isAutomationEnabled()) {
                param.fireUpdateFromTimeChange();
            }
        }
    }
}
