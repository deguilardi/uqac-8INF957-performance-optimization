package ca.uqac.performance.original;

import ca.uqac.performance.original.util.Sleeper;
import javafx.util.Pair;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static ca.uqac.performance.original.Config.*;

/**
 * A little user interface to display the transformers' loads
 */
public class Gui extends JFrame{

    private final static Gui instance = new Gui();
    private final List<JProgressBar> progressBars = new ArrayList<>(NUM_SUPPLIERS);
    private final JPanel barsPanel;

    /**
     * Constructor
     */
    private Gui(){
        super("Performance Improvement Stats");

        barsPanel = new JPanel();
        add(barsPanel);

        int height = 45 * NUM_SUPPLIERS / 2 ;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400,height);
        setVisible(true);
    }

    /**
     * Builds the user interface based on supplier pairs.
     * @param supplierPairs The supplier pairs.
     */
    public static void initGui(List<Pair<Transformer, Supplier>> supplierPairs){
        for(Pair<Transformer, Supplier> supplierPair: supplierPairs){
            instance.bindTransformer(supplierPair.getKey());
        }
        instance.barsPanel.revalidate();
        initWatchLoop(supplierPairs);
    }

    /**
     * Watch for suppliers changes.
     * Will update progress bars along with loads.
     * @param supplierPairs The supplier pairs.
     */
    private static void initWatchLoop(List<Pair<Transformer, Supplier>> supplierPairs) {
        new Thread(() -> {
            while (Sleeper.unsafeSleep(LOOP_INTERVAL * 10)) {
                for (Pair<Transformer, Supplier> pair : supplierPairs) {
                    Transformer transformer = pair.getKey();
                    JProgressBar progressBar = instance.progressBars.get(transformer.getId());
                    progressBar.setValue(transformer.getLoad());
                    switch (transformer.getState()){
                        case IDLE: progressBar.setString("idle"); break;
                        case OK: progressBar.setString("ok"); break;
                        case DANGER: progressBar.setString("danger"); break;
                        case MAX: progressBar.setString("max"); break;
                    }
                }
            }
        }).start();
    }

    /**
     * Bind a transformer to a progress bar.
     * @param transformer A transformer to be bind and watched.
     */
    private void bindTransformer(Transformer transformer) {
        JPanel panel = new JPanel();
        JProgressBar progressBar = new JProgressBar(0, TRANSFORMER_BUFFER_MAX);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        panel.add(progressBar);
        barsPanel.add(panel);
        progressBars.add(transformer.getId(), progressBar);
    }
}
