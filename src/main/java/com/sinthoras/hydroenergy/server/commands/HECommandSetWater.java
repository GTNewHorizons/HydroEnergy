package com.sinthoras.hydroenergy.server.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import com.sinthoras.hydroenergy.HE;
import com.sinthoras.hydroenergy.config.HEConfig;
import com.sinthoras.hydroenergy.server.HEServer;

public class HECommandSetWater extends CommandBase {

    @Override
    public String getCommandName() {
        return "hesetwater";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + getCommandName() + " <controllerId> <waterLevel>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] params) {
        boolean flag = true;
        if (params.length != 2) {
            flag = false;
        } else {
            try {
                float waterLevel = Float.parseFloat(params[1]);
                int controllerId = Integer.parseInt(params[0]);
                if (controllerId >= 0 || controllerId < HEConfig.maxDams) {
                    HEServer.instance.setWaterLevel(controllerId, waterLevel);
                    sender.addChatMessage(
                            new ChatComponentText(
                                    "Set water level of controller " + controllerId + " to " + waterLevel));
                    HE.info(
                            sender.getCommandSenderName() + " set water level of controller "
                                    + controllerId
                                    + " to "
                                    + waterLevel);
                } else {
                    flag = false;
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
