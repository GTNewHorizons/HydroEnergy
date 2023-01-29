package com.sinthoras.hydroenergy.server.commands;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import com.sinthoras.hydroenergy.server.HEServer;

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
