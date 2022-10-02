package com.sinthoras.hydroenergy.server.commands;

import com.sinthoras.hydroenergy.server.HEServer;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class HECommandListControllers extends CommandBase {

    @Override
    public String getCommandName() {
        return "helistcontrollers";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + getCommandName();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] params) {
        List<String> controllerList = HEServer.instance.getControllerCoordinates();
        sender.addChatMessage(new ChatComponentText("These are the controllers on this server:"));
        for (String controller : controllerList) {
            sender.addChatMessage(new ChatComponentText("    " + controller));
        }
    }
}
