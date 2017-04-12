/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Copyright 2011-2017 Peter Güttinger and contributors
 */
package ch.njol.skript.util;

import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.TriggerItem;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.concurrent.Executors;

/**
 * @author Andrew Tran
 */
public abstract class AsyncEffect extends Effect{
    private final Object lock = new Object();
    @Override
    @Nullable
    protected TriggerItem walk(Event e) {
        Delay.addDelayedEvent(e);
        new Thread(new Runnable() {
            @Override
            public void run() {
                execute(e);
                synchronized (lock){
                    lock.notify();
                }
            }
        }).start();
        synchronized (lock){
            try {
                lock.wait();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            TriggerItem next = getNext();
            if (next != null){
                TriggerItem.walk(next, e);
            }
        }
        return null;
    }
}
