package o.lartifa.jam.ob.event.events.local;

import o.lartifa.jam.ob.event.Event;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 此类由 Hykilpikonna 在 2018/08/24 创建!
 * Created by Hykilpikonna on 2018/08/24!
 * Github: https://github.com/hykilpikonna
 * QQ: admin@moecraft.cc -OR- 871674895
 *
 * @author Hykilpikonna
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class EventLocalException extends EventLocal
{
    private final Throwable exception;

    private final Event parentEvent;
}
