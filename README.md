# MiniDB

A minimal database built step-by-step to understand storage engine internals.

## Stages
1. Append-only log + in-memory index
2. Thread-safe writes
3. Binary record format
4. Segment files
5. Compaction
6. Comparison with B+Tree

Each stage is preserved using Git tags.

## Stage 1 — Thread-safe writes (Serialized append)

### Problem
The initial append-only storage engine was not safe under concurrent access.
If multiple threads attempted to write simultaneously, the following issues could occur:

- Two threads could observe the same file length and write at the same offset
- Interleaved writes could corrupt on-disk data
- The in-memory index could point to incorrect offsets
- Reads could seek to wrong locations, causing silent data corruption

These race conditions are non-deterministic and extremely difficult to debug.

---

### Solution
All write operations are now **serialized** using a dedicated write lock.
Only one thread may perform the following sequence at a time:

1. Seek to end of file
2. Append record
3. Publish the byte offset in the in-memory index

Reads remain concurrent and lock-free.

---

### Design Decisions
- A dedicated `writeLock` is used instead of `synchronized(this)` to avoid
  accidental lock contention and clearly express intent.
- The in-memory index uses `ConcurrentHashMap` to safely allow concurrent reads
  while writes are in progress.
- No durability or crash-safety guarantees are introduced at this stage;
  this stage focuses strictly on **thread-level correctness**.

---

### Invariant Introduced
> **No two writes can ever overlap in file space, and every published offset
> always points to a fully written record.**

All subsequent stages (binary records, segmentation, compaction) depend on this invariant.

---

### What This Stage Does NOT Solve
- Partial writes due to crashes
- Durability guarantees (`fsync`)
- Recovery from torn records
- On-disk format stability

These are intentionally deferred to later stages.

---

### Tests
- Concurrent write tests validate that multiple writers cannot corrupt the file
  or index.
- Read-after-write correctness is preserved under concurrent access.