package o.lartifa.jam.ob.event.events.local;

import o.lartifa.jam.ob.event.Event;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 本地事件
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public abstract class EventLocal extends Event
{
    protected boolean cancelled = false;
}