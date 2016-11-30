package de.mcmainiac.webconsole.server;

import java.util.ArrayList;

public class ChannelGroup extends ArrayList<Channel> {
    @Override
    public ChannelGroup clone() {
        super.clone();

        ChannelGroup clone = new ChannelGroup();
        clone.addAll(subList(0, size()));

        return clone;
    }
}
