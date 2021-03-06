package org.jf.dexlib2.builder.instruction;

import org.jf.dexlib2.builder.*;
import org.jf.dexlib2.iface.instruction.*;

import javax.annotation.*;

public class BuilderSwitchElement implements SwitchElement {
    private final int key;
    @Nonnull
    private final Label target;
    @Nonnull
    BuilderSwitchPayload parent;

    public BuilderSwitchElement(@Nonnull BuilderSwitchPayload parent,
                                int key,
                                @Nonnull Label target) {
        this.parent = parent;
        this.key = key;
        this.target = target;
    }

    @Override
    public int getKey() {
        return key;
    }

    @Override
    public int getOffset() {
        return target.getCodeAddress() - parent.getReferrer().getCodeAddress();
    }

    @Nonnull
    public Label getTarget() {
        return target;
    }
}
