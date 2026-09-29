import { useState } from 'react';
import { Eye, EyeOff, MessageSquareText, Star } from 'lucide-react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { api, ApiError } from '../lib/api';
import type { Page, Review, ReviewStatus } from '../types';
import { Empty, ErrorState } from '../components/States';

export default function ReviewModerationPage() {
  const [status, setStatus] = useState<ReviewStatus | ''>('');
  const queryClient = useQueryClient();
  const query = useQuery({
    queryKey: ['review-moderation', status],
    queryFn: () => api<Page<Review>>(`/api/reviews/moderation?${new URLSearchParams({ status, size: '50' })}`),
  });
  const moderate = useMutation({
    mutationFn: ({ review, nextStatus }: { review: Review; nextStatus: ReviewStatus }) => {
      const reason = nextStatus === 'HIDDEN' ? window.prompt('Reason for hiding this review:') : undefined;
      if (nextStatus === 'HIDDEN' && !reason?.trim()) throw new Error('A moderation reason is required');
      return api(`/api/reviews/${review.id}/moderate`, { method: 'POST', body: JSON.stringify({ status: nextStatus, reason }) });
    },
    onSuccess: () => { toast.success('Review moderation updated'); queryClient.invalidateQueries({ queryKey: ['review-moderation'] }); },
    onError: (error: Error) => toast.error(error instanceof ApiError ? error.message : error.message),
  });

  return (
    <section className="container-page py-14">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div><p className="eyebrow">Trust & quality</p><h1 className="mt-3 font-display text-4xl font-bold">Review moderation</h1><p className="mt-3 text-stone-600">Your queue is scoped by the server to courses you are allowed to manage.</p></div>
        <select className="input w-auto min-w-44" value={status} onChange={(event) => setStatus(event.target.value as ReviewStatus | '')}><option value="">All reviews</option><option value="PUBLISHED">Published</option><option value="HIDDEN">Hidden</option></select>
      </div>
      <div className="mt-8">
        {query.isLoading && <div className="skeleton h-64" />}
        {query.error && <ErrorState />}
        {!query.isLoading && !query.data?.content.length && <Empty title="The queue is clear" message="No reviews match this filter." />}
        <div className="grid gap-5 lg:grid-cols-2">
          {query.data?.content.map((review) => (
            <article key={review.id} className="card p-6">
              <div className="flex items-start justify-between gap-4"><div><p className="text-xs font-semibold uppercase tracking-wider text-forest">{review.courseTitle}</p><h2 className="mt-2 font-display text-xl font-semibold">{review.studentName}</h2></div><span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${review.status === 'PUBLISHED' ? 'bg-emerald-50 text-emerald-700' : 'bg-stone-100 text-stone-600'}`}>{review.status}</span></div>
              <div className="mt-4 flex text-amber-400">{Array.from({ length: 5 }).map((_, index) => <Star key={index} size={16} className={index < review.rating ? 'fill-amber-400' : 'text-stone-200'} />)}</div>
              <p className="mt-4 text-sm leading-6 text-stone-600">{review.comment}</p>
              {review.moderationReason && <p className="mt-4 rounded-xl bg-stone-50 p-3 text-xs text-stone-600"><strong>Reason:</strong> {review.moderationReason}</p>}
              <button disabled={moderate.isPending} onClick={() => moderate.mutate({ review, nextStatus: review.status === 'PUBLISHED' ? 'HIDDEN' : 'PUBLISHED' })} className={review.status === 'PUBLISHED' ? 'btn-secondary mt-5 text-red-700' : 'btn-primary mt-5'}>{review.status === 'PUBLISHED' ? <EyeOff size={16} /> : <Eye size={16} />}{review.status === 'PUBLISHED' ? 'Hide review' : 'Restore review'}</button>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
