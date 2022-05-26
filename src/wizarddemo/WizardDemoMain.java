
package wizarddemo;

import java.awt.EventQueue;
import java.awt.Rectangle;

import javax.swing.UIManager;

import org.netbeans.api.wizard.WizardDisplayer;

/**
 * Demo of a wizard that uses arbitrary panels rather than WizardPanel objects.
 * 
 */
public class WizardDemoMain
{
    public static void main (String[] ignored) throws Exception {
        //Use native L&F
        UIManager.setLookAndFeel (UIManager.getSystemLookAndFeelClassName());
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                WizardDisplayer.showWizard (new NewPetWizard().createWizard(), 
                        new Rectangle (20, 20, 300, 400));
                System.exit(0);
            }
        });
    }

}

