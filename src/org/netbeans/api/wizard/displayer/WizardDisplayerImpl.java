/*  The contents of this file are subject to the terms of the Common Development
 and Distribution License (the License). You may not use this file except in
 compliance with the License.
 You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 or http://www.netbeans.org/cddl.txt.
 When distributing Covered Code, include this CDDL Header Notice in each file
 and include the License file at http://www.netbeans.org/cddl.txt.
 If applicable, add the following below the CDDL Header, with the fields
 enclosed by brackets [] replaced by your own identifying information:

 Written by Stanley@StanleyKnutson.com based on code from Tim B.

 */

package org.netbeans.api.wizard.displayer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.RootPaneContainer;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.api.wizard.WizardResultReceiver;
import org.netbeans.modules.wizard.InstructionsPanelImpl;
import org.netbeans.modules.wizard.MergeMap;
import org.netbeans.modules.wizard.NbBridge;
import org.netbeans.spi.wizard.DeferredWizardResult;
import org.netbeans.spi.wizard.ResultProgressHandle;
import org.netbeans.spi.wizard.Summary;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardPanel;

/**
 * Default implementation of WizardDisplayer.
 * <b><i><font color="red">This class is NOT AN API CLASS.  There is no
 * commitment that it will remain backward compatible or even exist in the
 * future.  The API of this library is in the packages <code>org.netbeans.api.wizard</code>
 * and <code>org.netbeans.spi.wizard</code></font></i></b>.  <p>Use 
 * <code>WizardDisplayer.showWizard()</code> or its other static methods to
 * display wizards in a way which will continue to work over time.
 * @author stanley@StanleyKnutson.com
 * @author Tim Boudreau
 */
public class WizardDisplayerImpl extends WizardDisplayer
{


    ResultProgressHandle   progress       = null;

    JLabel                    ttlLabel       = null;

    JPanel                    ttlPanel       = null;

    Wizard                    wizard         = null;

    JPanel                    outerPanel     = null;

    NavButtonManager          buttonManager  = null;

    InstructionsPanel     instructions   = null;

    MergeMap                  settings       = null;

    JPanel                    inner          = null;

    JLabel                    problem        = null;

    Object                    wizardResult   = null;
    
    WizardResultReceiver      receiver       = null;

    /**
     * WizardPanel is the panel returned as the panel to display. Often a
     * subclass of WizardPanel
     */
    JComponent                wizardPanel    = null;

    boolean                   inSummary      = false;

    DeferredWizardResult      deferredResult = null;

    /**
     * Default constructor used by WizardDisplayer static methods.
     *
     */
    public WizardDisplayerImpl()
    {
    }
    
    protected void buildStepTitle()
    {
    	ttlLabel = new JLabel(wizard.getStepDescription(wizard.getAllSteps()[0]));
        if (helpAction == null) {
        	ttlLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                    .createEmptyBorder(5, 5, 12, 5), BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager
                    .getColor("textText")))); // NOI18N
        	ttlPanel = new JPanel()
            {
                public void doLayout()
                {
                    Dimension d = ttlLabel.getPreferredSize();
                    if (ttlLabel.getComponentOrientation() == ComponentOrientation.RIGHT_TO_LEFT)
                    {
                    	ttlLabel.setBounds(getWidth() - d.width, 0, getWidth(), d.height);
                    }
                    else
                    {
                    	ttlLabel.setBounds(0, 0, getWidth(), d.height);
                    }
                }

                public Dimension getPreferredSize()
                {
                    return ttlLabel.getPreferredSize();
                }
            };
            ttlPanel.add(ttlLabel);
        }
        else {
        	final JPanel ttlSubPanel = new JPanel(new BorderLayout());
        	URL url = getClass().getResource("help.png");
            Icon icon = new ImageIcon(url);
            final JButton helpButton = new JButton(helpAction);
            helpButton.setIcon(icon);
            ttlSubPanel.add(helpButton, BorderLayout.EAST);
            ttlSubPanel.add(ttlLabel, BorderLayout.WEST);
            ttlPanel = new JPanel()
            {
                public void doLayout()
                {
                    Dimension d = helpButton.getPreferredSize();
                    if (ttlSubPanel.getComponentOrientation() == ComponentOrientation.RIGHT_TO_LEFT)
                    {
                    	ttlSubPanel.setBounds(getWidth() - d.width, 0, getWidth(), d.height);
                    }
                    else
                    {
                    	ttlSubPanel.setBounds(0, 0, getWidth(), d.height);
                    }
                }

                public Dimension getPreferredSize()
                {
                    return helpButton.getPreferredSize();
                }
            };
            ttlPanel.add(ttlSubPanel);
        }
        
        Font f = ttlLabel.getFont();
        if (f == null)
        {
            f = UIManager.getFont("controlFont"); // NOI18N
        }
        if (f != null)
        {
            f = f.deriveFont(Font.BOLD);
            ttlLabel.setFont(f);
        }

    }
    
    JButton help;
    public void setHelp(JButton help) {
    	this.help = help;
    }
    
    public JButton getNext() {
    	return buttonManager.getNext();
    }
    
    public JButton getPrev() {
    	return buttonManager.getPrev();
    }
    
    public JButton getFinish() {
    	return buttonManager.getFinish();
    }
    
    public JButton getCancel() {
    	return buttonManager.getCancel();
    }
    
    public void processNext() {
    	buttonManager.processNext();
    }
    
    public void processFinish(ActionEvent event) {
    	buttonManager.processFinish(event);
    }
    
    public void processPrev() {
    	buttonManager.processPrev();
    }

    ArrayList<JButton> customButtons;
    /**
     * Show a wizard
     * 
     * @param awizard is the wizard to be displayed
     * @param bounds for display, may be null for default of 0,0,400,600. 
     * @param helpAction
     * @param initialProperties - initial values for the map
     * @return value of the 'finish' processing
     * @see org.netbeans.api.wizard.WizardDisplayer#show(org.netbeans.spi.wizard.Wizard, java.awt.Rectangle, javax.swing.Action, java.util.Map)
     */
    private JPanel createOuterPanel(final Wizard awizard, Rectangle bounds, Action helpAction,
                          Map initialProperties)
    {

        this.wizard = awizard;

        outerPanel = new JPanel();
        
        // apply default size
        // we don't enforce any maximum size
        if (bounds == null)
        {
            bounds = new Rectangle(0,0, 400,400);
        }

        if (wizard.getAllSteps().length == 0)
        {
            throw new IllegalArgumentException("Wizard has no steps"); // NOI18N
        }

        // initialize the ttl* stuff
        buildStepTitle();

        buttonManager = new NavButtonManager(this);
        buttonManager.setCustomButtons(customButtons);

        outerPanel.setLayout(new BorderLayout());
        
        Action kbdCancel = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                JButton b = buttonManager.getCancel();
                if (b.isEnabled() && receiver.canCancel() ) {
                    b.doClick();
                }
            }
        };
        outerPanel.getInputMap(outerPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        outerPanel.getActionMap().put("cancel", kbdCancel);

        if (instructions == null) {
        	 instructions = createInstructionsPanel();
        }

        buttonManager.buildButtons(helpAction);

        inner = new JPanel();
        inner.setLayout(new BorderLayout());
        //inner.add(ttlPanel, BorderLayout.NORTH);

        problem = new JLabel("  ");
        Color fg = UIManager.getColor("nb.errorColor"); // NOI18N
        problem.setForeground(fg == null ? Color.BLUE : fg);
        inner.add(problem, BorderLayout.SOUTH);
        problem.setPreferredSize(new Dimension(20, 20));

        outerPanel.add(instructions.getComponent(), BorderLayout.WEST);
        outerPanel.add(buttonManager.getButtons(), BorderLayout.SOUTH);
        outerPanel.add(inner, BorderLayout.CENTER);

        String first = wizard.getAllSteps()[0];
        settings = new MergeMap(first);

        // introduce the initial properties as if they had been set on page 1
        // even though they may be defaults for page 2
        if (initialProperties != null)
        {
            settings.putAll(initialProperties);
        }

        wizardPanel = wizard.navigatingTo(first, settings);
        String desc = wizard.getLongDescription (first);
        if (desc != null) {
            ttlLabel.setText (desc);
        }

        inner.add(wizardPanel, BorderLayout.CENTER);

        buttonManager.initializeNavigation();
        return outerPanel;
    }
    
    protected InstructionsPanel createInstructionsPanel() {
        return new InstructionsPanelImpl (wizard);
    }
    Action helpAction;
    public void install (Container c, Object layoutConstraint, Wizard awizard,
            Action helpAction, Map initialProperties, WizardResultReceiver receiver, InstructionsPanel instructionsPanel, ArrayList<JButton> customButtons) {    
    	this.helpAction = helpAction;
    	this.instructions = instructionsPanel;
    	this.customButtons = customButtons;
    	
        JPanel pnl = createOuterPanel (awizard, new Rectangle(), helpAction, initialProperties);       
        if (layoutConstraint != null) {
            if (c instanceof RootPaneContainer) {
                ((RootPaneContainer) c).getContentPane().add (pnl, layoutConstraint);
            } else {
                c.add (pnl, layoutConstraint);
            }
        } else {
            if (c instanceof RootPaneContainer) {
                ((RootPaneContainer) c).getContentPane().add (pnl);
            } else {
                c.add (pnl);
            }
        }
        this.receiver = receiver;
    }
    
    private static boolean warned;
    public Object show(final Wizard awizard, Rectangle bounds, Action helpAction,
                          Map initialProperties) {
        if (!EventQueue.isDispatchThread() && !warned) {
            Logger.getLogger(WizardDisplayerImpl.class.getName()).log(Level.WARNING, 
                    "show() should be called from the AWT Event Thread. This "
                    + "call may deadlock - c.f. "
                    + "http://java.net/jira/browse/WIZARD-33", new Throwable());
            warned = true;
        }
        createOuterPanel (awizard, bounds, helpAction, initialProperties);
        Object result = showInDialog(bounds);
        return result;
    }

    protected JDialog createDialog()
    {
        JDialog dlg;
        Object o = findLikelyOwnerWindow();
        if (o instanceof Frame)
        {
            dlg = new JDialog((Frame) o);
        }
        else if (o instanceof Dialog)
        {
            dlg = new JDialog((Dialog) o);
        }
        else
        {
            dlg = new JDialog();
        }
        return dlg;
    }

    protected Object showInDialog(Rectangle bounds)
    {
        // TODO: add flag for "showInFrame"

        JDialog dlg = createDialog();

        buttonManager.setWindow(dlg);

        dlg.setTitle(wizard.getTitle());

        dlg.getContentPane().setLayout(new BorderLayout());
        dlg.getContentPane().add(outerPanel, BorderLayout.CENTER);
        if (bounds != null)
        {
            dlg.setBounds(bounds);
        }
        else
        {
            dlg.pack();
        }
        dlg.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dlg.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                if (!(e.getWindow() instanceof JDialog)) {
                    return;
                }
                JDialog dlg = (JDialog) e.getWindow();
                boolean dontClose = false;
                if (!wizard.isBusy())
                {
                    DeferredWizardResult defResult;
                    synchronized(WizardDisplayerImpl.this) {
                        defResult = deferredResult;
                    }
                    try
                    {
                        if (defResult != null && defResult.canAbort())
                        {
                            defResult.abort();
                        }
                        else if (defResult != null && !defResult.canAbort())
                        {
                            dontClose = true;
                            return;
                        }
                    }
                    finally
                    {
                        if (!dontClose && wizard.cancel(settings))
                        {
                            dlg.setVisible(false);
                            dlg.dispose();
                        }
                    }
                }
            }
        });

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        if (bounds == null) {
            // XXX get screen insets?
            int x = (d.width - dlg.getWidth()) / 2;
            int y = (d.height - dlg.getHeight()) / 2;
            dlg.setLocation(x, y);
        }

        dlg.setModal(true);
        dlg.setVisible(true);

        return wizardResult;
    }

    private Window findLikelyOwnerWindow()
    {
        return KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
    }

    /**
     * Return the current wizard panel, or null if the currently displayed page
     * is not a WizardPanel.
     * 
     * @return
     */
    public WizardPanel getCurrentWizardPanel()
    {
        JComponent comp = wizardPanel;
        if (comp instanceof WizardPanel)
        {
            return (WizardPanel) comp;
        }
        return null;
    }

    public String getCurrentStep()
    {
        return settings.currID();
    }

    // available in the package only
    static void checkLegalNavMode(int i)
    {
        switch (i)
        {
            case Wizard.MODE_CAN_CONTINUE:
            case Wizard.MODE_CAN_CONTINUE_OR_FINISH:
            case Wizard.MODE_CAN_FINISH:
                return;
            default:
                throw new IllegalArgumentException("Illegal forward " + // NOI18N
                    "navigation mode: " + i); // NOI18N
        }
    }

    /*
     * private static final class LDlg extends JDialog { public LDlg() {
     *  } public LDlg (Frame frame) { super (frame); }
     * 
     * public LDlg (Dialog dlg) { super (dlg); }
     * 
     * public void setVisible (boolean val) { if (!val) { Thread.dumpStack(); }
     * super.setVisible (val); } }
     */

    /** 
     * Set the currently displayed panel.
     * @parm comp is can be anything - it is not required to be a WizardPage or WizardPanel 
     * */
    public void setCurrentWizardPanel(JComponent comp)
    {
        inner.add(comp, BorderLayout.CENTER);
        inner.remove(wizardPanel);
        wizardPanel = comp;
        inner.invalidate();
        inner.revalidate();
        inner.repaint();
        comp.requestFocus();
        if (!inSummary)
        {
            buttonManager.updateButtons();
        }
    }

    void handleSummary(Summary summary)
    {
        inSummary = true;
        instructions.setInSummaryPage(true);
        JComponent summaryComp = (JComponent) summary.getSummaryComponent(); // XXX
        if (summaryComp.getBorder() != null)
        {
            CompoundBorder b = new CompoundBorder(new EmptyBorder(5, 5, 5, 5), summaryComp
                .getBorder());
            summaryComp.setBorder(b);
        }
        setCurrentWizardPanel((JComponent) summaryComp); // XXX
        ttlLabel.setText(NbBridge.getString("org/netbeans/api/wizard/Bundle", // NOI18N
                                            WizardDisplayer.class, "Summary")); // NOI18N
        getButtonManager().setSummaryShowingMode();
        summaryComp.requestFocus();
        
    }

    protected ResultProgressHandle createProgressDisplay (boolean isUseBusy)
    {
        return new NavProgress(this, isUseBusy);
    }
    
    void handleDeferredWizardResult(final DeferredWizardResult r, final boolean inSummary)
    {
        synchronized (this) {
            deferredResult = r;
        }
        wizardPanel.setEnabled(false);
        progress = createProgressDisplay(r.isUseBusy());
        Container inst = instructions.getComponent();
        progress.addProgressComponents(inst);
        inst.invalidate();
        if (inst instanceof JComponent) {
            ((JComponent)inst).revalidate();
        }
        inst.repaint();
        Runnable run = new Runnable()
        {
            public void run()
            {
                if (!EventQueue.isDispatchThread())
                {
                    try
                    {
                        EventQueue.invokeLater (new Runnable() {
                            public void run() {
                            buttonManager.getWindow()
                                .setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                            }
                        });
                        r.start(settings, progress);
                        if (progress.isRunning())
                        {
                            progress.failed("Start method did not inidicate " +
                                    "failure or finished in " + r, false);
                        }
                        
                    }
                    finally
                    {
                        try
                        {
                            EventQueue.invokeAndWait(this);
                        }
                        catch (InvocationTargetException ex)
                        {
                            ex.printStackTrace();
                            return;
                        }
                        catch (InterruptedException ex)
                        {
                            ex.printStackTrace();
                            return;
                        }
                        finally
                        {
                            buttonManager.getWindow().setCursor(Cursor.getDefaultCursor());
                        }
                    }
                }
                else
                {
                    synchronized (this) {
                        deferredResult = null;
                    }
                    buttonManager.getCancel().setEnabled(true);
                    Container inst = instructions.getComponent();
                    inst.removeAll();
                    inst.invalidate();
                    if (inst instanceof JComponent) {
                        ((JComponent)instructions).revalidate();
                    }
                    inst.repaint();
                }
            }
        };
        Thread runner = new Thread(run, "Wizard Background Result Thread " + r); // NOI18N
        runner.start();
    }

    public void navigateTo(String id)
    {
        JComponent comp = wizard.navigatingTo(id, getSettings());
        String description = wizard.getLongDescription (id);
        if (description == null) {
            description = wizard.getStepDescription (id);
        }
        getTtlLabel().setText(description);
        setCurrentWizardPanel(comp);
    }

    public NavButtonManager getButtonManager()
    {
        return buttonManager;
    }

    public synchronized DeferredWizardResult getDeferredResult()
    {
        return deferredResult;
    }

    public InstructionsPanel getInstructions()
    {
        return instructions;
    }

    public boolean isInSummary()
    {
        return inSummary;
    }

    public void setInSummary(final boolean state)
    {
        inSummary = state;
        Runnable r = new Runnable() {
            public void run() {
                instructions.setInSummaryPage(state);
            }
        };
        if (EventQueue.isDispatchThread()) {
            r.run();
        } else {
            EventQueue.invokeLater (r);
        }
    }

    public JPanel getOuterPanel()
    {
        return outerPanel;
    }
    
    public JPanel getInnerPanel()
    {
        return inner;
    }

    public MergeMap getSettings()
    {
        return settings;
    }

    public JLabel getTtlLabel()
    {
        return ttlLabel;
    }

    public JPanel getTtlPanel()
    {
        return ttlPanel;
    }

    public Wizard getWizard()
    {
        return wizard;
    }

    public JComponent getWizardPanel()
    {
        return wizardPanel;
    }

    public Object getWizardResult()
    {
        return wizardResult;
    }

    public void setWizardResult(Object wizardResult)
    {
        this.wizardResult = wizardResult;
        if (receiver != null) {
            receiver.finished(wizardResult);
        }
    }

    public synchronized void setDeferredResult(DeferredWizardResult deferredResult)
    {
        this.deferredResult = deferredResult;
    }
    
    /**
     * Will only be called if there is a WizardResultReceiver - i.e. if the
     * wizard is being displayed in some kind of custom container.  Return
     * true to indicate we should not try to close the parent window.
     */ 
    boolean cancel() {
        boolean result = receiver != null;
        if (result) {
            receiver.cancelled(settings);
        }
        return result;
    }

    void updateProblem()
    {
        String prob = wizard.getProblem();
        problem.setText(prob == null ? " " : prob); // NOI18N
        if (prob != null && prob.trim().length() == 0)
        {
            // Issue 3 - provide ability to disable next w/o
            // showing the error line
            prob = null;
        }
        Border b = prob == null ? BorderFactory.createEmptyBorder(1, 0, 0, 0) : BorderFactory
            .createMatteBorder(1, 0, 0, 0, problem.getForeground());

        Border b1 = BorderFactory.createCompoundBorder(BorderFactory
            .createEmptyBorder(0, 12, 0, 12), b);

        problem.setBorder(b1);
    }

}
