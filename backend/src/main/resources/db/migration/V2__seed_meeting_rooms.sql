INSERT INTO meeting_room (name, location, capacity, amenities)
VALUES ('晨曦厅', '总部大厦 A 座 3F', 10,
        JSON_ARRAY('projector', 'whiteboard')),
       ('云海厅', '总部大厦 B 座 5F', 6,
        JSON_ARRAY('video', 'whiteboard')),
       ('星河厅', '研发中心 2F', 20,
        JSON_ARRAY('projector', 'video', 'whiteboard'));
