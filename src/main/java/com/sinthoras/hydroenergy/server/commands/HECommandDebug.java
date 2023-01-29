package com.sinthoras.hydroenergy.server.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.server.HEServer;

public class HECommandDebug extends CommandBase {

    @Override
    public String getCommandName() {
        return "hedebug";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return getCommandName() + " <id> <on|off>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] params) {
        boolean flag = true;
        if (params.length != 2) {
            flag = false;
        } else {
            try {
                int controllerId = Integer.parseInt(params[0]);
                boolean debugState = false;
                if (params[1].equalsIgnoreCase("on")) {
                    debugState = true;
                } else if (params[1].equalsIgnoreCase("off")) {
                    debugState = false;
                } else {
                    flag = false;
                }
                if (flag) {
                    HEServer.instance.setMode(controllerId, debugState ? HE.DamMode.DEBUG : HE.DamMode.SPREAD);
                    sender.addChatMessage(
                            new ChatComponentText(
                                    "Set controller " + controllerId + " to debug mode " + params[1].toUpperCase()));
                    HE.info(
                            sender.getCommandSenderName() + " set controller "
                                    + controllerId
                                    + " to debug mode "
                                    + params[1].toUpperCase());
                }
            } catch (Exception ex) {
                flag = false;
            }
        }
        if (!flag) {
            sender.addChatMessage(new ChatComponentText("Could not parse command!\n" + getCommandUsage(null)));
        }
    }
}
