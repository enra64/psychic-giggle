/**
 * This package contains all command types, so subclasses of {@link de.ovgu.softwareprojekt.control.commands.Command} like
 * {@link de.ovgu.softwareprojekt.control.commands.SetSensorCommand}. Commands are identified by their {@link de.ovgu.softwareprojekt.control.commands.CommandType}
 * member function {@link de.ovgu.softwareprojekt.control.commands.Command#getCommandType()} so they can be easily cast to the respective subclass.
 */
package de.ovgu.softwareprojekt.control.commands;