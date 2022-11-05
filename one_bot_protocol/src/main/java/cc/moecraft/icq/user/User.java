package cc.moecraft.icq.user;

import cc.moecraft.icq.sender.returndata.returnpojo.get.RStrangerInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 此类由 Hykilpikonna 在 2018/05/26 创建!
 * Created by Hykilpikonna on 2018/05/26!
 * Github: https://github.com/hykilpikonna
 * QQ: admin@moecraft.cc -OR- 871674895
 *
 * @author Hykilpikonna
 */
@Getter
@RequiredArgsConstructor
public class User {
    private final long id;

    private RStrangerInfo info;
}
