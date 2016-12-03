package de.mcmainiac.webconsole.server;

import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.Set;

public class WconCommandSender implements RemoteConsoleCommandSender {
    private static final String NAME = "WCON";

    private org.bukkit.Server bukkitServer;
    private Server wconServer;

    public WconCommandSender(org.bukkit.Server bukkitServer, Server wconServer) {
        this.bukkitServer = bukkitServer;
        this.wconServer = wconServer;
    }

    @Override
    public void sendMessage(String s) {
        for (Channel channel : wconServer.getChannels()) {
            if (!channel.isConnected())
                continue;

            try {
                channel.sendMessage(s);
            } catch (IOException e) {
                wconServer.exceptionOccurred(e, false, false);
            }
        }
    }

    @Override
    public void sendMessage(String[] strings) {
        for (String message : strings)
            sendMessage(message);
    }

    @Override
    public org.bukkit.Server getServer() {
        return bukkitServer;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isPermissionSet(String s) {
        return false;
    }

    @Override
    public boolean isPermissionSet(Permission permission) {
        return false;
    }

    @Override
    public boolean hasPermission(String s) {
        return false;
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return false;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b) {
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i) {
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int i) {
        return null;
    }

    @Override
    public void removeAttachment(PermissionAttachment permissionAttachment) {

    }

    @Override
    public void recalculatePermissions() {

    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return null;
    }

    @Override
    public boolean isOp() {
        return false;
    }

    @Override
    public void setOp(boolean b) {

    }
}
