package ru.practicum.shareit.user.storage;


/*
@Repository
public class InMemoryUserRepository implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1L;

    @Override
    public User save(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("User not found with id: " + user.getId());
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void deleteById(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("User not found with id: " + id);
        }
        users.remove(id);
    }
}
*/