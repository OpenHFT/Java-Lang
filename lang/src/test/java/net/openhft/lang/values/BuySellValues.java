package net.openhft.lang.values;

import net.openhft.lang.model.constraints.MaxSize;

/**
 * Created by peter.lawrey on 06/08/2015.
 */
public interface BuySellValues {
    BuySell getValue();

    void setValue(@MaxSize(5) BuySell buySell);
}
