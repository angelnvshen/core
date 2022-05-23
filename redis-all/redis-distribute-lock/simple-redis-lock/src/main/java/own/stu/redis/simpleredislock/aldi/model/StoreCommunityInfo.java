package own.stu.redis.simpleredislock.aldi.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class StoreCommunityInfo implements Serializable {
    private String code;

    private String nameCN;

    private String nameUS;

    private Integer limit;

    private Integer status;

}
