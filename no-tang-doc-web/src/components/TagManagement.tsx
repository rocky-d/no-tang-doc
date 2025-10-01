import React, { useState } from 'react';
import { Plus, Edit, Trash2, Tag } from 'lucide-react';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from './ui/dialog';
import { Badge } from './ui/badge';
import { Label } from './ui/label';
import { toast } from 'sonner@2.0.3';

interface Tag {
  id: string;
  name: string;
  color: string;
  description?: string;
  documentCount: number;
}

const mockTags: Tag[] = [
  { id: '1', name: 'Important', color: '#ef4444', description: 'High priority documents', documentCount: 12 },
  { id: '2', name: 'Work', color: '#3b82f6', description: 'Work-related documents', documentCount: 25 },
  { id: '3', name: 'Personal', color: '#10b981', description: 'Personal documents', documentCount: 8 },
  { id: '4', name: 'Archive', color: '#6b7280', description: 'Archived documents', documentCount: 45 },
  { id: '5', name: 'Client', color: '#f59e0b', description: 'Client documents', documentCount: 18 },
];

const tagColors = [
  { name: 'Red', value: '#ef4444' },
  { name: 'Blue', value: '#3b82f6' },
  { name: 'Green', value: '#10b981' },
  { name: 'Yellow', value: '#f59e0b' },
  { name: 'Purple', value: '#8b5cf6' },
  { name: 'Pink', value: '#ec4899' },
  { name: 'Gray', value: '#6b7280' },
  { name: 'Orange', value: '#f97316' },
];

interface TagManagementProps {
  searchTerm?: string;
}

export function TagManagement({ searchTerm = '' }: TagManagementProps) {
  const [tags, setTags] = useState<Tag[]>(mockTags);
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [editingTag, setEditingTag] = useState<Tag | null>(null);
  const [formData, setFormData] = useState({
    name: '',
    color: '#3b82f6',
    description: ''
  });

  const handleCreateTag = () => {
    if (!formData.name.trim()) {
      toast.error('Tag name is required');
      return;
    }

    const newTag: Tag = {
      id: Date.now().toString(),
      name: formData.name.trim(),
      color: formData.color,
      description: formData.description.trim(),
      documentCount: 0
    };

    setTags([...tags, newTag]);
    setFormData({ name: '', color: '#3b82f6', description: '' });
    setIsCreateDialogOpen(false);
    toast.success('Tag created successfully');
  };

  const handleUpdateTag = () => {
    if (!editingTag || !formData.name.trim()) {
      toast.error('Tag name is required');
      return;
    }

    setTags(tags.map(tag => 
      tag.id === editingTag.id 
        ? {
            ...tag,
            name: formData.name.trim(),
            color: formData.color,
            description: formData.description.trim()
          }
        : tag
    ));

    setEditingTag(null);
    setFormData({ name: '', color: '#3b82f6', description: '' });
    toast.success('Tag updated successfully');
  };

  const handleDeleteTag = (tag: Tag) => {
    if (tag.documentCount > 0) {
      toast.error(`Cannot delete tag "${tag.name}" as it's being used by ${tag.documentCount} documents`);
      return;
    }

    setTags(tags.filter(t => t.id !== tag.id));
    toast.success('Tag deleted successfully');
  };

  const openEditDialog = (tag: Tag) => {
    setEditingTag(tag);
    setFormData({
      name: tag.name,
      color: tag.color,
      description: tag.description || ''
    });
  };

  const resetForm = () => {
    setFormData({ name: '', color: '#3b82f6', description: '' });
    setEditingTag(null);
  };

  // Filter tags based on search term
  const filteredTags = tags.filter(tag => 
    tag.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    tag.description?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">Tag Management</h1>
          <p className="text-muted-foreground">Create, edit, and organize your document tags</p>
        </div>
        
        <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
          <DialogTrigger asChild>
            <Button onClick={() => setIsCreateDialogOpen(true)}>
              <Plus className="w-4 h-4 mr-2" />
              Create Tag
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Create New Tag</DialogTitle>
              <DialogDescription>
                Add a new tag to organize your documents
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              <div>
                <Label htmlFor="tagName">Tag Name</Label>
                <Input
                  id="tagName"
                  placeholder="Enter tag name"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                />
              </div>
              
              <div>
                <Label htmlFor="tagColor">Color</Label>
                <div className="flex flex-wrap gap-2 mt-2">
                  {tagColors.map((color) => (
                    <button
                      key={color.value}
                      className={`w-8 h-8 rounded-full border-2 ${
                        formData.color === color.value ? 'border-foreground' : 'border-transparent'
                      }`}
                      style={{ backgroundColor: color.value }}
                      onClick={() => setFormData({ ...formData, color: color.value })}
                      title={color.name}
                    />
                  ))}
                </div>
              </div>
              
              <div>
                <Label htmlFor="tagDescription">Description (Optional)</Label>
                <Input
                  id="tagDescription"
                  placeholder="Enter tag description"
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                />
              </div>
              
              <div className="flex justify-end space-x-2">
                <Button variant="outline" onClick={() => { setIsCreateDialogOpen(false); resetForm(); }}>
                  Cancel
                </Button>
                <Button onClick={handleCreateTag}>Create Tag</Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {filteredTags.length > 0 ? (
          filteredTags.map((tag) => (
            <Card key={tag.id}>
              <CardHeader className="pb-3">
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-2">
                    <div 
                      className="w-3 h-3 rounded-full"
                      style={{ backgroundColor: tag.color }}
                    />
                    <CardTitle className="text-lg">{tag.name}</CardTitle>
                  </div>
                  <div className="flex space-x-1">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => openEditDialog(tag)}
                    >
                      <Edit className="w-4 h-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleDeleteTag(tag)}
                      disabled={tag.documentCount > 0}
                    >
                      <Trash2 className="w-4 h-4" />
                    </Button>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                {tag.description && (
                  <p className="text-sm text-muted-foreground mb-2">{tag.description}</p>
                )}
                <Badge variant="secondary">
                  {tag.documentCount} document{tag.documentCount !== 1 ? 's' : ''}
                </Badge>
              </CardContent>
            </Card>
          ))
        ) : (
          <div className="col-span-full text-center py-8">
            <p className="text-muted-foreground">No tags found matching "{searchTerm}"</p>
          </div>
        )}
      </div>

      {/* Edit Dialog */}
      <Dialog open={!!editingTag} onOpenChange={(open) => !open && setEditingTag(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Edit Tag</DialogTitle>
            <DialogDescription>
              Update the tag information
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div>
              <Label htmlFor="editTagName">Tag Name</Label>
              <Input
                id="editTagName"
                placeholder="Enter tag name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              />
            </div>
            
            <div>
              <Label htmlFor="editTagColor">Color</Label>
              <div className="flex flex-wrap gap-2 mt-2">
                {tagColors.map((color) => (
                  <button
                    key={color.value}
                    className={`w-8 h-8 rounded-full border-2 ${
                      formData.color === color.value ? 'border-foreground' : 'border-transparent'
                    }`}
                    style={{ backgroundColor: color.value }}
                    onClick={() => setFormData({ ...formData, color: color.value })}
                    title={color.name}
                  />
                ))}
              </div>
            </div>
            
            <div>
              <Label htmlFor="editTagDescription">Description (Optional)</Label>
              <Input
                id="editTagDescription"
                placeholder="Enter tag description"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              />
            </div>
            
            <div className="flex justify-end space-x-2">
              <Button variant="outline" onClick={() => { setEditingTag(null); resetForm(); }}>
                Cancel
              </Button>
              <Button onClick={handleUpdateTag}>Update Tag</Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}