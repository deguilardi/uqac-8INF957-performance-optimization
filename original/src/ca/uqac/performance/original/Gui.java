package ca.uqac.performance.original;

import ca.uqac.performance.original.util.Sleeper;
import javafx.util.Pair;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static ca.uqac.performance.original.Config.*;

public class Gui extends JFrame{

    private static Gui instance;
    private List<JProgressBar> progressBars = new ArrayList<>(NUM_SUPPLIERS);
    private JPanel barsPanel;

    private Gui(){
        super("Performance Improvement Stats");

        Integer height = 45 * NUM_SUPPLIERS / 2 ;

        barsPanel = new JPanel();
        add(barsPanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400,height);
        setVisible(true);
    }

    public static void initGui(List<Pair<Transformer, Supplier>> suppliers){
        instance = new Gui();
        for(Pair<Transformer, Supplier> pair: suppliers){
            instance.addTransformer(pair.getKey());
        }
        instance.barsPanel.revalidate();

        new Thread(() -> {
            while (true) {
                if (!Sleeper.unsafeSleep(LOOP_INTERVAL * 10)) {
                    break;
                }
                for (Pair<Transformer, Supplier> pair : suppliers) {
                    Transformer transformer = pair.getKey();
                    JProgressBar progressBar = instance.progressBars.get(transformer.getId());
                    progressBar.setValue(transformer.getLoad());
                }
            }
        }).start();
    }

    private void addTransformer(Transformer transformer) {
        JPanel panel = new JPanel();

        JProgressBar progressBar = new JProgressBar(0, TRANSFORMER_BUFFER_MAX);
        progressBar.setValue(0);
        panel.add(progressBar);
        barsPanel.add(panel);

        progressBars.add(transformer.getId(), progressBar);
    }
}
