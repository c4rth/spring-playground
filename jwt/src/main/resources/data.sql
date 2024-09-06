-- truncate table `users_roles`;
-- truncate table `users`;
-- truncate table `roles`;

insert into `roles` (`id`, `name`)
values (1, 'ROLE_ADMIN'),
       (2, 'ROLE_USER');

insert into `users` (`id`, `name`, `username`, `email`, `password`)
values (1, 'John', 'john', 'john@gmail.com', '$2a$10$IeofhAYT3lUfrF0bi1aflOat.IU3xOkZWaAWAuVc9jO2.QxTtH4RO'), // password = password
       (2, 'Paul', 'paul', 'paul@gmail.com', '$2a$10$IeofhAYT3lUfrF0bi1aflOat.IU3xOkZWaAWAuVc9jO2.QxTtH4RO'), // password = password
       (3, 'Mark', 'mark', 'mark@gmail.com', '$2a$10$IeofhAYT3lUfrF0bi1aflOat.IU3xOkZWaAWAuVc9jO2.QxTtH4RO'); // password = password

insert into `users_roles` (`user_id`, `role_id`)
values (1,1),
       (2,2),
       (3,1),
       (3,2);