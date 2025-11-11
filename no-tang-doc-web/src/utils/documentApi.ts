/**
 * Document API utilities (deprecated direct HTTP)
 *
 * This module now delegates to the Repository layer.
 * Prefer importing from `repositories/DocumentRepository` in new code.
 */

import { getDocumentRepository } from '../repositories/DocumentRepository';
export type { Document } from '../repositories/DocumentRepository';

/**
 * Advanced search function that calls your backend API (delegated)
 */
export const advancedSearch = async (query: string) => {
  const repo = getDocumentRepository();
  return repo.advancedSearch(query);
};

/**
 * Get all documents from backend and map to UI shape (delegated)
 */
export const getAllDocuments = async () => {
  const repo = getDocumentRepository();
  return repo.getAll();
};

/**
 * Example function for searching documents by tags (delegated)
 */
export const searchByTags = async (tags: string[]) => {
  const repo = getDocumentRepository();
  return repo.searchByTags(tags);
};